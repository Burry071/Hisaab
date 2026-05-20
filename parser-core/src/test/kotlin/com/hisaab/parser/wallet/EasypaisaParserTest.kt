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
 * All tests MUST FAIL until EasypaisaParser is implemented.
 *
 * Real Easypaisa SMS formats:
 *  DEBIT  : "PKR 2,000 sent to Usman Khan (0341XXXXXXX) from your Easypaisa account. Fee: PKR 0. Ref: EP20260315001"
 *  CREDIT : "PKR 5,000 received in your Easypaisa account from Hamid (0321XXXXXXX). Balance: PKR 12,000. Ref: EP20260315002"
 *  BILLPAY: "Your SSGC bill of PKR 1,800 has been paid. Easypaisa balance: PKR 6,200. TxID: EP20260315003"
 *  TOPUP  : "PKR 3,000 added to your Easypaisa account. New balance: PKR 9,000. Ref: EP20260315004"
 *  WITHDRAW: "PKR 500 withdrawn from your Easypaisa account at Agent 1234. Balance: PKR 8,500."
 */
class EasypaisaParserTest {

    private lateinit var parser: EasypaisaParser
    private val SENDER = "Easypaisa"
    private val TIMESTAMP = 1747091341000L

    @Before
    fun setUp() {
        parser = EasypaisaParser()
    }

    // ─── canParse ───────────────────────────────────────────────────────────

    @Test fun `canParse returns true for Easypaisa sender`() {
        assertTrue(parser.canParse(SENDER, "Any body"))
    }

    @Test fun `canParse returns true for EP sender alias`() {
        assertTrue(parser.canParse("EP", "PKR 100 sent"))
    }

    @Test fun `canParse returns false for non-Easypaisa sender`() {
        assertFalse(parser.canParse("MCB", "Some body"))
    }

    // ─── DEBIT / Send ───────────────────────────────────────────────────────

    @Test fun `parses send money SMS as DEBIT`() {
        val sms = "PKR 2,000 sent to Usman Khan (0341XXXXXXX) from your Easypaisa account. Fee: PKR 0. Ref: EP20260315001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("2000"), result.amount)
    }

    @Test fun `parses send money SMS counterparty correctly`() {
        val sms = "PKR 2,000 sent to Usman Khan (0341XXXXXXX) from your Easypaisa account. Fee: PKR 0. Ref: EP20260315001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals("Usman Khan", result.counterparty)
    }

    @Test fun `parses send money reference number correctly`() {
        val sms = "PKR 2,000 sent to Usman Khan (0341XXXXXXX) from your Easypaisa account. Fee: PKR 0. Ref: EP20260315001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals("EP20260315001", result.referenceNumber)
    }

    // ─── CREDIT / Receive ───────────────────────────────────────────────────

    @Test fun `parses receive money SMS as CREDIT`() {
        val sms = "PKR 5,000 received in your Easypaisa account from Hamid (0321XXXXXXX). Balance: PKR 12,000. Ref: EP20260315002"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("5000"), result.amount)
        assertEquals(BigDecimal("12000"), result.balanceAfter)
    }

    @Test fun `parses credit counterparty sender name`() {
        val sms = "PKR 5,000 received in your Easypaisa account from Hamid (0321XXXXXXX). Balance: PKR 12,000. Ref: EP20260315002"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals("Hamid", result.counterparty)
    }

    // ─── BILL PAYMENT ───────────────────────────────────────────────────────

    @Test fun `parses bill payment SMS as BILL_PAYMENT`() {
        val sms = "Your SSGC bill of PKR 1,800 has been paid. Easypaisa balance: PKR 6,200. TxID: EP20260315003"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("1800"), result.amount)
        assertEquals("SSGC", result.counterparty)
        assertEquals(BigDecimal("6200"), result.balanceAfter)
    }

    // ─── TOP-UP ─────────────────────────────────────────────────────────────

    @Test fun `parses top-up SMS as TOP_UP`() {
        val sms = "PKR 3,000 added to your Easypaisa account. New balance: PKR 9,000. Ref: EP20260315004"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.TOP_UP, result.type)
        assertEquals(BigDecimal("3000"), result.amount)
        assertEquals(BigDecimal("9000"), result.balanceAfter)
    }

    // ─── Metadata ───────────────────────────────────────────────────────────

    @Test fun `sets institution to Easypaisa`() {
        val sms = "PKR 100 sent to X. Ref: EP001"
        assertEquals("Easypaisa", parser.parse(SENDER, sms, TIMESTAMP).institution)
    }

    @Test fun `sets ingestion source to SMS`() {
        val sms = "PKR 100 sent to X. Ref: EP001"
        assertEquals(IngestionSource.SMS, parser.parse(SENDER, sms, TIMESTAMP).source)
    }

    @Test fun `confidence score is at least 0_8 for standard SMS`() {
        val sms = "PKR 2,000 sent to Usman Khan (0341XXXXXXX) from your Easypaisa account. Fee: PKR 0. Ref: EP20260315001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)
        assertTrue(result.confidenceScore >= 0.8f)
    }
}
