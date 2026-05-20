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
 * All tests MUST FAIL until JazzCashParser is implemented.
 *
 * Real JazzCash SMS formats collected from Pakistani users:
 *  DEBIT  : "You have sent Rs.5,000 to Ali Raza (0312XXXXXXX). Your JazzCash balance is Rs.1,200. Ref#TXN20260315143201"
 *  CREDIT : "You have received Rs.2,500 from Bilal (0333XXXXXXX). Your JazzCash balance is Rs.7,300. Ref#TXN20260315101500"
 *  TOPUP  : "Your JazzCash account has been loaded with Rs.10,000. Balance: Rs.11,500. Ref#TOP20260315090000"
 *  BILLPAY: "LESCO bill of Rs.3,200 paid successfully. JazzCash balance: Rs.8,100. Ref#BILL20260315080000"
 *  AGENT  : "Amount Rs.500 deposited by Agent. Balance: Rs.9,000."
 */
class JazzCashParserTest {

    private lateinit var parser: JazzCashParser
    private val SENDER = "JazzCash"
    private val TIMESTAMP = 1747091341000L  // arbitrary fixed epoch

    @Before
    fun setUp() {
        parser = JazzCashParser()
    }

    // ─── canParse ───────────────────────────────────────────────────────────

    @Test fun `canParse returns true for JazzCash sender`() {
        assertTrue(parser.canParse(SENDER, "Any body"))
    }

    @Test fun `canParse returns false for non-JazzCash sender`() {
        assertFalse(parser.canParse("HBL", "Some random body"))
    }

    @Test fun `canParse returns true for JAZZ sender alias`() {
        assertTrue(parser.canParse("JAZZ", "You have sent Rs.100"))
    }

    // ─── DEBIT / Send ───────────────────────────────────────────────────────

    @Test fun `parses send money SMS as DEBIT with correct amount`() {
        val sms = "You have sent Rs.5,000 to Ali Raza (0312XXXXXXX). Your JazzCash balance is Rs.1,200. Ref#TXN20260315143201"
        val result: ParsedTransaction = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("5000"), result.amount)
    }

    @Test fun `parses send money SMS counterparty name correctly`() {
        val sms = "You have sent Rs.5,000 to Ali Raza (0312XXXXXXX). Your JazzCash balance is Rs.1,200. Ref#TXN20260315143201"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals("Ali Raza", result.counterparty)
    }

    @Test fun `parses send money SMS balance after correctly`() {
        val sms = "You have sent Rs.5,000 to Ali Raza (0312XXXXXXX). Your JazzCash balance is Rs.1,200. Ref#TXN20260315143201"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(BigDecimal("1200"), result.balanceAfter)
    }

    @Test fun `parses send money SMS reference number correctly`() {
        val sms = "You have sent Rs.5,000 to Ali Raza (0312XXXXXXX). Your JazzCash balance is Rs.1,200. Ref#TXN20260315143201"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals("TXN20260315143201", result.referenceNumber)
    }

    // ─── CREDIT / Receive ───────────────────────────────────────────────────

    @Test fun `parses receive money SMS as CREDIT with correct amount`() {
        val sms = "You have received Rs.2,500 from Bilal (0333XXXXXXX). Your JazzCash balance is Rs.7,300. Ref#TXN20260315101500"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("2500"), result.amount)
    }

    @Test fun `parses receive money SMS counterparty sender name`() {
        val sms = "You have received Rs.2,500 from Bilal (0333XXXXXXX). Your JazzCash balance is Rs.7,300. Ref#TXN20260315101500"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals("Bilal", result.counterparty)
    }

    // ─── TOP-UP ─────────────────────────────────────────────────────────────

    @Test fun `parses account top-up SMS as TOP_UP type`() {
        val sms = "Your JazzCash account has been loaded with Rs.10,000. Balance: Rs.11,500. Ref#TOP20260315090000"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.TOP_UP, result.type)
        assertEquals(BigDecimal("10000"), result.amount)
        assertEquals(BigDecimal("11500"), result.balanceAfter)
    }

    // ─── BILL PAYMENT ───────────────────────────────────────────────────────

    @Test fun `parses bill payment SMS as BILL_PAYMENT type`() {
        val sms = "LESCO bill of Rs.3,200 paid successfully. JazzCash balance: Rs.8,100. Ref#BILL20260315080000"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("3200"), result.amount)
        assertEquals("LESCO", result.counterparty)
    }

    // ─── Metadata ───────────────────────────────────────────────────────────

    @Test fun `sets institution to JazzCash`() {
        val sms = "You have sent Rs.100 to X. Balance: Rs.50. Ref#TXN000"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals("JazzCash", result.institution)
    }

    @Test fun `sets ingestion source to SMS`() {
        val sms = "You have sent Rs.100 to X. Balance: Rs.50. Ref#TXN000"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals(IngestionSource.SMS, result.source)
    }

    @Test fun `confidence score is at least 0_8 for standard JazzCash SMS`() {
        val sms = "You have sent Rs.5,000 to Ali Raza (0312XXXXXXX). Your JazzCash balance is Rs.1,200. Ref#TXN20260315143201"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertTrue("Expected confidence >= 0.8, got ${result.confidenceScore}", result.confidenceScore >= 0.8f)
    }

    @Test fun `id is deterministic for same sender body and timestamp`() {
        val sms = "You have sent Rs.5,000 to Ali Raza. Balance: Rs.1,200. Ref#TXN001"
        val r1 = parser.parse(SENDER, sms, TIMESTAMP)
        val r2 = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals(r1.id, r2.id)
    }

    @Test fun `handles comma-separated amounts correctly`() {
        val sms = "You have sent Rs.1,50,000 to Company (0300XXXXXXX). Balance: Rs.20,000. Ref#TXN999"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals(BigDecimal("150000"), result.amount)
    }
}
