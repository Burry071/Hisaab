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
 * All tests MUST FAIL until NayaPayParser is implemented.
 *
 * Real NayaPay SMS formats:
 *  DEBIT  : "You've sent PKR 3,500 to Sara Ahmed. Available balance: PKR 11,200. Txn ID: NP-20260315-001"
 *  CREDIT : "PKR 8,000 received from Kamran Butt. New balance: PKR 19,500. Txn ID: NP-20260315-002"
 *  BILLPAY: "IESCO bill payment of PKR 2,100 successful. Balance: PKR 17,400. Txn ID: NP-20260315-003"
 *  REQUEST : "Payment request of PKR 500 from Hina Malik fulfilled. Balance: PKR 16,900."
 */
class NayaPayParserTest {

    private lateinit var parser: NayaPayParser
    private val SENDER = "NayaPay"
    private val TIMESTAMP = 1747091341000L

    @Before
    fun setUp() {
        parser = NayaPayParser()
    }

    // ─── canParse ───────────────────────────────────────────────────────────

    @Test fun `canParse returns true for NayaPay sender`() {
        assertTrue(parser.canParse(SENDER, "Any body"))
    }

    @Test fun `canParse returns true for NAYAPAY uppercase sender`() {
        assertTrue(parser.canParse("NAYAPAY", "You've sent PKR 100"))
    }

    @Test fun `canParse returns false for unrelated sender`() {
        assertFalse(parser.canParse("UBL", "Some body"))
    }

    // ─── DEBIT ──────────────────────────────────────────────────────────────

    @Test fun `parses send money SMS as DEBIT with correct amount`() {
        val sms = "You've sent PKR 3,500 to Sara Ahmed. Available balance: PKR 11,200. Txn ID: NP-20260315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("3500"), result.amount)
    }

    @Test fun `parses send money SMS counterparty correctly`() {
        val sms = "You've sent PKR 3,500 to Sara Ahmed. Available balance: PKR 11,200. Txn ID: NP-20260315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals("Sara Ahmed", result.counterparty)
    }

    @Test fun `parses balance after correctly in send SMS`() {
        val sms = "You've sent PKR 3,500 to Sara Ahmed. Available balance: PKR 11,200. Txn ID: NP-20260315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals(BigDecimal("11200"), result.balanceAfter)
    }

    @Test fun `parses NayaPay-format Txn ID correctly`() {
        val sms = "You've sent PKR 3,500 to Sara Ahmed. Available balance: PKR 11,200. Txn ID: NP-20260315-001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals("NP-20260315-001", result.referenceNumber)
    }

    // ─── CREDIT ─────────────────────────────────────────────────────────────

    @Test fun `parses receive money SMS as CREDIT`() {
        val sms = "PKR 8,000 received from Kamran Butt. New balance: PKR 19,500. Txn ID: NP-20260315-002"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("8000"), result.amount)
        assertEquals("Kamran Butt", result.counterparty)
    }

    // ─── BILL PAYMENT ───────────────────────────────────────────────────────

    @Test fun `parses bill payment SMS as BILL_PAYMENT`() {
        val sms = "IESCO bill payment of PKR 2,100 successful. Balance: PKR 17,400. Txn ID: NP-20260315-003"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("2100"), result.amount)
        assertEquals("IESCO", result.counterparty)
    }

    // ─── Metadata ───────────────────────────────────────────────────────────

    @Test fun `sets institution to NayaPay`() {
        val sms = "PKR 100 received from X. Balance: PKR 200. Txn ID: NP-001"
        assertEquals("NayaPay", parser.parse(SENDER, sms, TIMESTAMP).institution)
    }

    @Test fun `confidence score at least 0_8 for standard SMS`() {
        val sms = "You've sent PKR 3,500 to Sara Ahmed. Available balance: PKR 11,200. Txn ID: NP-20260315-001"
        assertTrue(parser.parse(SENDER, sms, TIMESTAMP).confidenceScore >= 0.8f)
    }
}
