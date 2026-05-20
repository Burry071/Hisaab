package com.hisaab.domain.agents

import com.hisaab.domain.llm.LlmPromptLibrary
import com.hisaab.domain.llm.LlmService
import com.hisaab.domain.model.*
import com.hisaab.parser.model.ParsedTransaction
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Wave 1 — ContradictionAgent (UPDATED — LLM-arbitrated resolution)
 *
 * Detection logic (deterministic, always runs):
 *   1. Exact ID duplicate
 *   2. Fuzzy duplicate (same amount + institution + time window)
 *   3. Cross-source amount mismatch (different amount, same probable transaction)
 *
 * Resolution logic:
 *   • Exact duplicates → deterministic (discard incoming)
 *   • Amount mismatches → LLM arbitrates WHICH amount is correct and WHY
 *   • LLM is optional — if null, falls back to "bank is authoritative" heuristic
 *
 * Tool logged: detect_contradiction
 */
class ContradictionAgent(
    private val llmService: LlmService? = null,
    private val duplicateWindowMs: Long = 5 * 60 * 1000,
    private val amountTolerancePct: Double = 0.01,
) {
    private val dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.of("Asia/Karachi"))

    suspend fun run(
        incoming: List<ParsedTransaction>,
        existing: List<ParsedTransaction>,
        trace: AgentTrace,
    ): List<ConflictResult> {
        if (incoming.isEmpty()) return emptyList()

        val conflicts       = mutableListOf<ConflictResult>()
        val existingById    = existing.associateBy { it.id }

        for (tx in incoming) {
            trace.step(AgentName.CONTRADICTION, "detect_contradiction",
                "Scanning ${tx.institution} ${tx.type} PKR ${tx.amount}",
                toolCall = "detect_contradiction(id=${tx.id.take(8)}, window=${duplicateWindowMs}ms)",
                status = AgentTaskStatus.RUNNING)

            // ── 1. Exact ID duplicate ──────────────────────────────────────────
            if (existingById.containsKey(tx.id)) {
                val conflict = ConflictResult(
                    type                = ConflictType.DUPLICATE,
                    severity            = ConflictSeverity.HIGH,
                    description         = "Exact duplicate (ID match): ${tx.institution} PKR ${tx.amount}",
                    incomingId          = tx.id,
                    conflictingId       = tx.id,
                    suggestedResolution = "DISCARD_INCOMING — existing record is authoritative.",
                    canonicalAmount     = tx.amount,
                    resolvedByLlm       = false,
                )
                conflicts.add(conflict)
                trace.step(AgentName.CONTRADICTION, "detect_contradiction",
                    "⚠️ DUPLICATE — ${tx.institution} PKR ${tx.amount}",
                    toolResult = "DUPLICATE severity=HIGH resolution=DISCARD_INCOMING",
                    status = AgentTaskStatus.CONFLICT_FOUND)
                continue
            }

            // ── 2. Fuzzy duplicate ────────────────────────────────────────────
            val fuzzy = existing.firstOrNull { ex ->
                ex.institution == tx.institution &&
                    ex.type == tx.type &&
                    ex.amount == tx.amount &&
                    abs(ex.timestampEpochMs - tx.timestampEpochMs) < duplicateWindowMs
            }
            if (fuzzy != null) {
                val conflict = ConflictResult(
                    type                = ConflictType.DUPLICATE,
                    severity            = ConflictSeverity.MEDIUM,
                    description         = "Probable duplicate: same amount+institution within ${duplicateWindowMs / 1000}s",
                    incomingId          = tx.id,
                    conflictingId       = fuzzy.id,
                    suggestedResolution = "DISCARD_INCOMING — likely same SMS delivered twice.",
                    canonicalAmount     = tx.amount,
                    resolvedByLlm       = false,
                )
                conflicts.add(conflict)
                trace.step(AgentName.CONTRADICTION, "detect_contradiction",
                    "⚠️ FUZZY DUPLICATE — ${tx.institution} PKR ${tx.amount}",
                    toolResult = "DUPLICATE severity=MEDIUM",
                    status = AgentTaskStatus.CONFLICT_FOUND)
                continue
            }

            // ── 3. Cross-source amount mismatch ───────────────────────────────
            val crossSource = existing.firstOrNull { ex ->
                ex.institution != tx.institution &&
                    ex.type == tx.type &&
                    abs(ex.timestampEpochMs - tx.timestampEpochMs) < duplicateWindowMs &&
                    pctDiff(ex.amount, tx.amount) > amountTolerancePct &&
                    pctDiff(ex.amount, tx.amount) < 0.20  // same ballpark (< 20% diff)
            }
            if (crossSource != null) {
                val diff = tx.amount.subtract(crossSource.amount).abs()

                // ── LLM arbitration for amount mismatch ────────────────────────
                val llmResolution = if (llmService != null) {
                    trace.step(AgentName.CONTRADICTION, "detect_contradiction",
                        "Calling LLM to arbitrate: ${tx.institution} vs ${crossSource.institution}",
                        toolCall = "detect_contradiction(arbitrate=llm, diff=PKR $diff)",
                        status = AgentTaskStatus.RUNNING)
                    arbitrateWithLlm(tx, crossSource, diff)
                } else {
                    heuristicArbitrate(tx, crossSource)
                }

                val conflict = ConflictResult(
                    type                = ConflictType.AMOUNT_MISMATCH,
                    severity            = ConflictSeverity.HIGH,
                    description         = "Amount mismatch: ${tx.institution}=PKR ${tx.amount} " +
                        "vs ${crossSource.institution}=PKR ${crossSource.amount} (Δ PKR $diff)",
                    incomingId          = tx.id,
                    conflictingId       = crossSource.id,
                    suggestedResolution = llmResolution.decision,
                    canonicalAmount     = llmResolution.canonicalAmount,
                    arbitrationReasoning= llmResolution.reasoning,
                    resolvedByLlm       = llmService != null,
                )
                conflicts.add(conflict)
                trace.step(AgentName.CONTRADICTION, "detect_contradiction",
                    "⚠️ AMOUNT MISMATCH — Δ PKR $diff | ${llmResolution.decision}",
                    toolResult = "AMOUNT_MISMATCH confidence=${llmResolution.confidence}",
                    status = AgentTaskStatus.CONFLICT_FOUND)
                continue
            }

            // Clean
            trace.step(AgentName.CONTRADICTION, "detect_contradiction",
                "✅ CLEAN — ${tx.institution} ${tx.type} PKR ${tx.amount}",
                toolResult = "CLEAN",
                status = AgentTaskStatus.DONE)
        }

        return conflicts
    }

    // ── LLM arbitration ───────────────────────────────────────────────────────

    private data class ArbitrationResult(
        val decision: String,
        val canonicalAmount: BigDecimal,
        val reasoning: String,
        val confidence: Float,
    )

    private suspend fun arbitrateWithLlm(
        txA: ParsedTransaction,
        txB: ParsedTransaction,
        diff: BigDecimal,
    ): ArbitrationResult {
        return try {
            val response = llmService!!.complete(
                systemPrompt = LlmPromptLibrary.CONTRADICTION_SYSTEM,
                userPrompt   = LlmPromptLibrary.contradictionUser(
                    sourceA      = txA.institution,
                    amountA      = txA.amount.toLong(),
                    timestampA   = dtFormatter.format(Instant.ofEpochMilli(txA.timestampEpochMs)),
                    sourceB      = txB.institution,
                    amountB      = txB.amount.toLong(),
                    timestampB   = dtFormatter.format(Instant.ofEpochMilli(txB.timestampEpochMs)),
                    description  = "${txA.type.name} ~PKR ${txA.amount}",
                ),
                maxTokens = 200,
            )
            parseLlmArbitration(response.content, txA.amount, txB.amount)
        } catch (e: Exception) {
            heuristicArbitrate(txA, txB)  // fallback on LLM failure
        }
    }

    private fun parseLlmArbitration(
        raw: String,
        amountA: BigDecimal,
        amountB: BigDecimal,
    ): ArbitrationResult {
        val lines = raw.lines().associate { line ->
            val idx = line.indexOf(":")
            if (idx < 0) "" to "" else line.substring(0, idx).trim().uppercase() to line.substring(idx + 1).trim()
        }
        val decision    = lines["DECISION"]         ?: "FLAG_MANUAL"
        val canonicalStr= lines["CANONICAL_AMOUNT"]
        val reasoning   = lines["REASONING"]        ?: "LLM arbitration"
        val confidence  = lines["CONFIDENCE"]?.toFloatOrNull() ?: 0.7f

        val canonical = when {
            decision == "USE_A" -> amountA
            decision == "USE_B" -> amountB
            canonicalStr != null -> canonicalStr.replace(",","").toBigDecimalOrNull() ?: amountA
            else -> amountA
        }
        return ArbitrationResult(decision, canonical, reasoning, confidence)
    }

    /** Heuristic: bank SMS > wallet SMS > notification */
    private fun heuristicArbitrate(txA: ParsedTransaction, txB: ParsedTransaction): ArbitrationResult {
        val bankInstitutions = setOf("HBL", "MCB", "UBL", "Meezan", "Bank Alfalah", "Allied", "NBP")
        val aIsBank = txA.institution in bankInstitutions
        val bIsBank = txB.institution in bankInstitutions
        return when {
            aIsBank && !bIsBank -> ArbitrationResult(
                "USE_A", txA.amount,
                "Bank SMS (${txA.institution}) is more authoritative than wallet (${txB.institution})",
                0.80f,
            )
            bIsBank && !aIsBank -> ArbitrationResult(
                "USE_B", txB.amount,
                "Bank SMS (${txB.institution}) is more authoritative than wallet (${txA.institution})",
                0.80f,
            )
            else -> ArbitrationResult(
                "FLAG_MANUAL", txA.amount,
                "Both sources are same tier — manual review required",
                0.50f,
            )
        }
    }

    private fun pctDiff(a: BigDecimal, b: BigDecimal): Double {
        if (a.signum() == 0 && b.signum() == 0) return 0.0
        val larger = if (a > b) a else b
        return a.subtract(b).abs().toDouble() / larger.toDouble()
    }

    private fun BigDecimal.toLong() = this.toLong()
}
