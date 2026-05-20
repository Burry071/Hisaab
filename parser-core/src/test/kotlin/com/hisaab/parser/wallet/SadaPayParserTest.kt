package com.hisaab.parser.wallet

import com.hisaab.parser.model.ParsedTransaction
import com.hisaab.parser.model.TransactionType
import com.hisaab.parser.model.IngestionSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * TDD — RED PHASE
 * All tests MUST FAIL until SadaPayParser is implemented.
 *
 * Real SadaPay SMS formats (SadaPay uses WhatsApp primarily but also SMS):
 *  DEBIT  : "You sent PKR 1,200 to Aisha Farooq. Your SadaPay balance is PKR 4,800. ID: SP-TXN-2026-0315-001"
 *  CREDIT : "PKR 6,000 received from Zara Khan. SadaPay balance: PKR 10,800. ID: SP-TXN-2026-0315-002"
 *  TOPUP  : "Your SadaPay card has been loaded with PKR 5,000. Balance: PKR 9,800. ID: SP-TOP-2026-0315-001"
 *  PURCHASE: "Purchase of PKR 850 at Carrefour. Balance: PKR 8,950. ID: SP-PUR-2026-0315-001"
 */
class SadaPayParserTest {

    private lateinit var parser: SadaPayParser
    private val SENDER = "SadaPay"
    private val TIMESTAMP = 1747091341000L

    @Before
    fun setUp() {
        parser = SadaPayParser()
    }

    // ─── canParse ───────────────────────────────────────────────────────────

    @Test fun `canParse returns true for SadaPay sender`() {
        assertTrue(parser.canParse(SENDER, "Any body"))
    }

    @Test fun `canParse returns true for SADAPAY uppercase sender`() {
        assertTrue(parser.canParse("SADAPAY", "You sent PKR 100"))
    }

    @Test fun `canParse returns false for unrelated sender`() {
        assertFalse(parser.canParse("Meezan", "Some body"))
    }

    // ─── DEBIT / Send ───────────────────────────────────────────────────────

    @Test fun `parses send SMS as DEBIT with correct amount`() {
        val sms = "You sent PKR 1,200 to Aisha Farooq. Your SadaPay balance is PKR 4,800. ID: SP-TXN-2026-0315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("1200"), result.amount)
    }

    @Test fun `parses send SMS counterparty correctly`() {
        val sms = "You sent PKR 1,200 to Aisha Farooq. Your SadaPay balance is PKR 4,800. ID: SP-TXN-2026-0315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals("Aisha Farooq", result.counterparty)
    }

    @Test fun `parses SadaPay balance after correctly`() {
        val sms = "You sent PKR 1,200 to Aisha Farooq. Your SadaPay balance is PKR 4,800. ID: SP-TXN-2026-0315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals(BigDecimal("4800"), result.balanceAfter)
    }

    @Test fun `parses SadaPay ID correctly`() {
        val sms = "You sent PKR 1,200 to Aisha Farooq. Your SadaPay balance is PKR 4,800. ID: SP-TXN-2026-0315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals("SP-TXN-2026-0315-001", result.referenceNumber)
    }

    // ─── CREDIT ─────────────────────────────────────────────────────────────

    @Test fun `parses receive SMS as CREDIT`() {
        val sms = "PKR 6,000 received from Zara Khan. SadaPay balance: PKR 10,800. ID: SP-TXN-2026-0315-002"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("6000"), result.amount)
        assertEquals("Zara Khan", result.counterparty)
        assertEquals(BigDecimal("10800"), result.balanceAfter)
    }

    // ─── PURCHASE (Debit Card) ──────────────────────────────────────────────

    @Test fun `parses purchase SMS as DEBIT`() {
        val sms = "Purchase of PKR 850 at Carrefour. Balance: PKR 8,950. ID: SP-PUR-2026-0315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("850"), result.amount)
        assertEquals("Carrefour", result.counterparty)
    }

    // ─── TOP-UP ─────────────────────────────────────────────────────────────

    @Test fun `parses card load SMS as TOP_UP`() {
        val sms = "Your SadaPay card has been loaded with PKR 5,000. Balance: PKR 9,800. ID: SP-TOP-2026-0315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.TOP_UP, result.type)
        assertEquals(BigDecimal("5000"), result.amount)
        assertEquals(BigDecimal("9800"), result.balanceAfter)
    }

    // ─── Metadata ───────────────────────────────────────────────────────────

    @Test fun `sets institution to SadaPay`() {
        val sms = "You sent PKR 100 to X. Balance: PKR 50. ID: SP-001"
        assertEquals("SadaPay", parser.parse(SENDER, sms, TIMESTAMP).institution)
    }

    @Test fun `confidence score at least 0_8 for standard SMS`() {
        val sms = "You sent PKR 1,200 to Aisha Farooq. Your SadaPay balance is PKR 4,800. ID: SP-TXN-2026-0315-001"
        assertTrue(parser.parse(SENDER, sms, TIMESTAMP).confidenceScore >= 0.8f)
    }
}
