package com.hisaab.parser

import com.hisaab.parser.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ParserOrchestratorTest {

    private lateinit var orchestrator: ParserOrchestrator
    private val ts = 1_700_000_000_000L

    @Before fun setUp() { orchestrator = ParserOrchestrator() }

    // ── Routing tests ─────────────────────────────────────────────────────────

    @Test fun `routes JazzCash SMS to JazzCashParser`() {
        val body = "You have sent Rs.3,000 to Ali. Balance: Rs.17,000."
        val result = orchestrator.parse("JazzCash", body, ts)
        assertEquals("JazzCash", result.institution)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test fun `routes HBL SMS to HBLParser`() {
        val body = "Rs.5,000 has been debited from your HBL account. Available Balance: Rs.45,000."
        val result = orchestrator.parse("HBL", body, ts)
        assertEquals("HBL", result.institution)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test fun `routes UBL SMS to UBLParser`() {
        val body = "PKR 8,000 debited from A/C XXXX3344. Bal: PKR 42,000."
        val result = orchestrator.parse("UBL", body, ts)
        assertEquals("UBL", result.institution)
    }

    @Test fun `routes MCB SMS to MCBParser`() {
        val body = "Your A/C XXXX4321 debited with Rs.7,500. Available Balance: Rs.32,500."
        val result = orchestrator.parse("MCB", body, ts)
        assertEquals("MCB", result.institution)
    }

    @Test fun `routes Meezan SMS to MeezanBankParser`() {
        val body = "PKR 6,500 debited from your Meezan A/C. Bal: PKR 43,500."
        val result = orchestrator.parse("MEEZAN", body, ts)
        assertEquals("Meezan Bank", result.institution)
    }

    @Test fun `routes Alfalah SMS to BankAlfalahParser`() {
        val body = "PKR 9,000 has been debited from your Alfalah account. Balance: PKR 41,000."
        val result = orchestrator.parse("ALFALAH", body, ts)
        assertEquals("Bank Alfalah", result.institution)
    }

    // ── Tier 3 escalation ─────────────────────────────────────────────────────

    @Test fun `unrecognised sender returns UNKNOWN and requires Tier 3`() {
        val body = "Your OTP is 123456. Do not share."
        val result = orchestrator.parse("UNKNOWN_BANK", body, ts)
        assertEquals(TransactionType.UNKNOWN, result.type)
        assertTrue(orchestrator.requiresTier3Fallback(result))
    }

    @Test fun `low-confidence result requires Tier 3`() {
        val body = "Dear customer, your account has been updated."
        val result = orchestrator.parse("HBL", body, ts)
        assertTrue(orchestrator.requiresTier3Fallback(result))
    }

    @Test fun `high-confidence result does not require Tier 3`() {
        val body = "Rs.5,000 has been debited from your HBL account. Available Balance: Rs.45,000."
        val result = orchestrator.parse("HBL", body, ts)
        assertFalse(orchestrator.requiresTier3Fallback(result))
    }

    // ── Idempotency across parsers ─────────────────────────────────────────────

    @Test fun `same SMS routed twice produces same ID`() {
        val body = "Rs.5,000 has been debited from your HBL account. Bal: Rs.45,000."
        val id1 = orchestrator.parse("HBL", body, ts).id
        val id2 = orchestrator.parse("HBL", body, ts).id
        assertEquals(id1, id2)
    }

    // ── Diagnostics ───────────────────────────────────────────────────────────

    @Test fun `diagnose returns parser name for known sender`() {
        val name = orchestrator.diagnose("HBL", "Rs.1,000 debited.")
        assertEquals("HBLParser", name)
    }

    @Test fun `diagnose returns escalation message for unknown sender`() {
        val name = orchestrator.diagnose("RANDOM_BANK", "some text")
        assertTrue(name.contains("Tier 3"))
    }

    // ── Currency invariant ────────────────────────────────────────────────────

    @Test fun `all parsers produce PKR currency`() {
        val pairs = listOf(
            "JazzCash" to "You have sent Rs.1,000 to X. Bal: Rs.9,000.",
            "Easypaisa" to "Rs.1,000 sent to X. Balance: Rs.9,000.",
            "HBL"       to "Rs.1,000 has been debited from your HBL account. Bal: Rs.9,000.",
            "UBL"       to "PKR 1,000 debited from A/C XXXX. Bal: PKR 9,000.",
            "MCB"       to "Your A/C debited with Rs.1,000. Bal: Rs.9,000.",
            "MEEZAN"    to "PKR 1,000 debited from your Meezan A/C. Bal: PKR 9,000.",
            "ALFALAH"   to "PKR 1,000 has been debited from your Alfalah A/C. Bal: PKR 9,000.",
        )
        pairs.forEach { (sender, body) ->
            assertEquals(
                "Expected PKR for $sender",
                "PKR",
                orchestrator.parse(sender, body, ts).currency,
            )
        }
    }
}
