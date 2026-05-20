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
 * All tests MUST FAIL until UPaisaParser is implemented.
 *
 * Real UPaisa SMS formats:
 *  DEBIT  : "Rs.1,000 transferred to Adeel (0314XXXXXXX). Ref No: UP20260315001. UPaisa Balance: Rs.4,200"
 *  CREDIT : "Rs.3,500 received from Waqar Ali. UPaisa Balance: Rs.7,700. Ref: UP20260315002"
 *  BILLPAY: "Sui Gas bill Rs.900 paid via UPaisa. Balance: Rs.6,800. Ref: UP20260315003"
 *  TOPUP  : "Rs.2,000 loaded to UPaisa. Balance: Rs.8,800. Ref: UP20260315004"
 */
class UPaisaParserTest {

    private lateinit var parser: UPaisaParser
    private val SENDER = "UPaisa"
    private val TIMESTAMP = 1747091341000L

    @Before fun setUp() { parser = UPaisaParser() }

    @Test fun `canParse returns true for UPaisa sender`() {
        assertTrue(parser.canParse(SENDER, "Any body"))
    }

    @Test fun `canParse returns false for unrelated sender`() {
        assertFalse(parser.canParse("HBL", "Any body"))
    }

    @Test fun `parses transfer SMS as DEBIT`() {
        val sms = "Rs.1,000 transferred to Adeel (0314XXXXXXX). Ref No: UP20260315001. UPaisa Balance: Rs.4,200"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("1000"), result.amount)
        assertEquals(BigDecimal("4200"), result.balanceAfter)
        assertEquals("Adeel", result.counterparty)
        assertEquals("UP20260315001", result.referenceNumber)
    }

    @Test fun `parses received SMS as CREDIT`() {
        val sms = "Rs.3,500 received from Waqar Ali. UPaisa Balance: Rs.7,700. Ref: UP20260315002"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("3500"), result.amount)
        assertEquals("Waqar Ali", result.counterparty)
    }

    @Test fun `parses bill payment as BILL_PAYMENT`() {
        val sms = "Sui Gas bill Rs.900 paid via UPaisa. Balance: Rs.6,800. Ref: UP20260315003"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("900"), result.amount)
        assertEquals("Sui Gas", result.counterparty)
    }

    @Test fun `parses top-up as TOP_UP`() {
        val sms = "Rs.2,000 loaded to UPaisa. Balance: Rs.8,800. Ref: UP20260315004"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.TOP_UP, result.type)
        assertEquals(BigDecimal("2000"), result.amount)
        assertEquals(BigDecimal("8800"), result.balanceAfter)
    }

    @Test fun `sets institution to UPaisa`() {
        val sms = "Rs.100 received from X. UPaisa Balance: Rs.200. Ref: UP001"
        assertEquals("UPaisa", parser.parse(SENDER, sms, TIMESTAMP).institution)
    }

    @Test fun `confidence score at least 0_8 for standard SMS`() {
        val sms = "Rs.1,000 transferred to Adeel (0314XXXXXXX). Ref No: UP20260315001. UPaisa Balance: Rs.4,200"
        assertTrue(parser.parse(SENDER, sms, TIMESTAMP).confidenceScore >= 0.8f)
    }
}
