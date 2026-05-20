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
 * All tests MUST FAIL until ZindigiParser is implemented.
 *
 * Real Zindigi SMS formats (Zindigi is part of Jazz/Jazz Cash ecosystem):
 *  DEBIT  : "PKR 2,000 debited from your Zindigi account. Transferred to Faraz (0323XXXXXXX). Balance: PKR 6,500. ID: ZND20260315001"
 *  CREDIT : "PKR 4,500 credited to your Zindigi account from Mariam. Balance: PKR 11,000. ID: ZND20260315002"
 *  PURCHASE: "PKR 1,200 debited for purchase at Gul Ahmed. Zindigi balance: PKR 9,800. ID: ZND20260315003"
 *  TOPUP  : "PKR 5,000 added to your Zindigi wallet. New balance: PKR 14,800. ID: ZND20260315004"
 */
class ZindigiParserTest {

    private lateinit var parser: ZindigiParser
    private val SENDER = "Zindigi"
    private val TIMESTAMP = 1747091341000L

    @Before fun setUp() { parser = ZindigiParser() }

    @Test fun `canParse returns true for Zindigi sender`() {
        assertTrue(parser.canParse(SENDER, "Any body"))
    }

    @Test fun `canParse returns true for ZINDIGI uppercase sender`() {
        assertTrue(parser.canParse("ZINDIGI", "PKR 100 debited"))
    }

    @Test fun `canParse returns false for Jazz sender (different parser)`() {
        assertFalse(parser.canParse("JazzCash", "Any body"))
    }

    @Test fun `parses debit transfer SMS as DEBIT`() {
        val sms = "PKR 2,000 debited from your Zindigi account. Transferred to Faraz (0323XXXXXXX). Balance: PKR 6,500. ID: ZND20260315001"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("2000"), result.amount)
        assertEquals(BigDecimal("6500"), result.balanceAfter)
        assertEquals("Faraz", result.counterparty)
        assertEquals("ZND20260315001", result.referenceNumber)
    }

    @Test fun `parses credit SMS as CREDIT`() {
        val sms = "PKR 4,500 credited to your Zindigi account from Mariam. Balance: PKR 11,000. ID: ZND20260315002"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("4500"), result.amount)
        assertEquals("Mariam", result.counterparty)
        assertEquals(BigDecimal("11000"), result.balanceAfter)
    }

    @Test fun `parses purchase SMS as DEBIT`() {
        val sms = "PKR 1,200 debited for purchase at Gul Ahmed. Zindigi balance: PKR 9,800. ID: ZND20260315003"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("1200"), result.amount)
        assertEquals("Gul Ahmed", result.counterparty)
    }

    @Test fun `parses top-up SMS as TOP_UP`() {
        val sms = "PKR 5,000 added to your Zindigi wallet. New balance: PKR 14,800. ID: ZND20260315004"
        val result = parser.parse(SENDER, sms, TIMESTAMP)

        assertEquals(TransactionType.TOP_UP, result.type)
        assertEquals(BigDecimal("5000"), result.amount)
        assertEquals(BigDecimal("14800"), result.balanceAfter)
    }

    @Test fun `sets institution to Zindigi`() {
        val sms = "PKR 100 credited from X. Balance: PKR 200. ID: ZND001"
        assertEquals("Zindigi", parser.parse(SENDER, sms, TIMESTAMP).institution)
    }

    @Test fun `confidence score at least 0_8 for standard SMS`() {
        val sms = "PKR 2,000 debited from your Zindigi account. Transferred to Faraz (0323XXXXXXX). Balance: PKR 6,500. ID: ZND20260315001"
        assertTrue(parser.parse(SENDER, sms, TIMESTAMP).confidenceScore >= 0.8f)
    }
}
