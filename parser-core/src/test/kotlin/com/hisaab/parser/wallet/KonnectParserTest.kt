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
 * All tests MUST FAIL until KonnectParser is implemented.
 *
 * Real Konnect by HBL SMS formats:
 *  DEBIT  : "PKR 1,500 sent to Sajid via Konnect. Remaining balance: PKR 3,800. Ref: KC20260315001"
 *  CREDIT : "PKR 7,000 received in your Konnect account from Omar. Balance: PKR 10,800. Ref: KC20260315002"
 *  BILLPAY: "Electricity bill PKR 2,500 paid via Konnect. Balance: PKR 8,300. Ref: KC20260315003"
 *  TOPUP  : "PKR 4,000 deposited in your Konnect account. Balance: PKR 12,300. Ref: KC20260315004"
 *  WITHDRAWAL: "PKR 500 withdrawn from Konnect. Balance: PKR 11,800. Ref: KC20260315005"
 */
class KonnectParserTest {

    private lateinit var parser: KonnectParser
    private val SENDER = "Konnect"
    private val TIMESTAMP = 1747091341000L

    @Before fun setUp() { parser = KonnectParser() }

    @Test fun `canParse returns true for Konnect sender`() {
        assertTrue(parser.canParse(SENDER, "Any body"))
    }

    @Test fun `canParse returns true for KONNECT uppercase sender`() {
        assertTrue(parser.canParse("KONNECT", "PKR 100 sent"))
    }

    @Test fun `canParse returns false for HBL sender (different parser)`() {
        // Konnect by HBL is a distinct wallet; HBL bank SMS uses a different parser
        assertFalse(parser.canParse("HBL", "Any body"))
    }

    @Test fun `parses send SMS as DEBIT`() {
        val sms = "PKR 1,500 sent to Sajid via Konnect. Remaining balance: PKR 3,800. Ref: KC20260315001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("1500"), result.amount)
        assertEquals(BigDecimal("3800"), result.balanceAfter)
        assertEquals("Sajid", result.counterparty)
        assertEquals("KC20260315001", result.referenceNumber)
    }

    @Test fun `parses receive SMS as CREDIT`() {
        val sms = "PKR 7,000 received in your Konnect account from Omar. Balance: PKR 10,800. Ref: KC20260315002"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("7000"), result.amount)
        assertEquals("Omar", result.counterparty)
        assertEquals(BigDecimal("10800"), result.balanceAfter)
    }

    @Test fun `parses bill payment as BILL_PAYMENT`() {
        val sms = "Electricity bill PKR 2,500 paid via Konnect. Balance: PKR 8,300. Ref: KC20260315003"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("2500"), result.amount)
        assertEquals("Electricity", result.counterparty)
    }

    @Test fun `parses deposit as TOP_UP`() {
        val sms = "PKR 4,000 deposited in your Konnect account. Balance: PKR 12,300. Ref: KC20260315004"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.TOP_UP, result.type)
        assertEquals(BigDecimal("4000"), result.amount)
        assertEquals(BigDecimal("12300"), result.balanceAfter)
    }

    @Test fun `sets institution to Konnect`() {
        val sms = "PKR 100 sent to X via Konnect. Remaining balance: PKR 50. Ref: KC001"
        assertEquals("Konnect", parser.parse(SENDER, sms, TIMESTAMP).institution)
    }

    @Test fun `confidence score at least 0_8 for standard SMS`() {
        val sms = "PKR 1,500 sent to Sajid via Konnect. Remaining balance: PKR 3,800. Ref: KC20260315001"
        assertTrue(parser.parse(SENDER, sms, TIMESTAMP).confidenceScore >= 0.8f)
    }

    @Test fun `id is deterministic for same inputs`() {
        val sms = "PKR 1,500 sent to Sajid via Konnect. Remaining balance: PKR 3,800. Ref: KC20260315001"
        val r1 = parser.parse(SENDER, sms, TIMESTAMP)
        val r2 = parser.parse(SENDER, sms, TIMESTAMP)
        assertEquals(r1.id, r2.id)
    }
}
