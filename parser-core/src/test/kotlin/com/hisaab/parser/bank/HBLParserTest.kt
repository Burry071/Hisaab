package com.hisaab.parser.bank

import com.hisaab.parser.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class HBLParserTest {

    private lateinit var parser: HBLParser
    private val ts = 1_700_000_000_000L  // fixed epoch ms for deterministic IDs

    @Before fun setUp() { parser = HBLParser() }

    // ── canParse ──────────────────────────────────────────────────────────────

    @Test fun `canParse returns true for HBL sender`() =
        assertTrue(parser.canParse("HBL", ""))

    @Test fun `canParse returns true for HBLBANK sender`() =
        assertTrue(parser.canParse("HBLBANK", ""))

    @Test fun `canParse returns true for HBL-BANK sender`() =
        assertTrue(parser.canParse("HBL-BANK", ""))

    @Test fun `canParse returns false for non-HBL sender`() =
        assertFalse(parser.canParse("JazzCash", ""))

    // ── DEBIT ─────────────────────────────────────────────────────────────────

    @Test fun `parse debit SMS returns DEBIT type with correct amount`() {
        val body = "Rs.5,000 has been debited from your HBL account XXXX1234. Available Balance: Rs.45,000. Ref: TXN987654"
        val result = parser.parse("HBL", body, ts)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("5000"), result.amount)
        assertEquals("HBL", result.institution)
    }

    @Test fun `parse debit SMS extracts available balance`() {
        val body = "Rs.1,500 has been debited from your HBL account. Available Balance: Rs.28,500."
        val result = parser.parse("HBL", body, ts)
        assertEquals(BigDecimal("28500"), result.balanceAfter)
    }

    @Test fun `parse debit SMS extracts reference number`() {
        val body = "Rs.2,000 has been debited from your HBL account. Available Balance: Rs.10,000. Ref: TXN112233"
        val result = parser.parse("HBL", body, ts)
        assertEquals("TXN112233", result.referenceNumber)
    }

    // ── CREDIT ────────────────────────────────────────────────────────────────

    @Test fun `parse credit SMS returns CREDIT type with correct amount`() {
        val body = "Rs.15,000 has been credited to your HBL account XXXX5678. Available Balance: Rs.65,000."
        val result = parser.parse("HBL", body, ts)
        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("15000"), result.amount)
    }

    @Test fun `parse credit SMS with large comma-formatted amount`() {
        val body = "Rs.1,50,000 has been credited to your HBL account. Available Balance: Rs.2,50,000."
        val result = parser.parse("HBL", body, ts)
        assertEquals(BigDecimal("150000"), result.amount)
        assertEquals(BigDecimal("250000"), result.balanceAfter)
    }

    // ── TRANSFER ──────────────────────────────────────────────────────────────

    @Test fun `parse transfer SMS returns TRANSFER type`() {
        val body = "Amount Rs.10,000 transferred to Ali Khan (03001234567). Available Balance: Rs.40,000. Ref: TXN445566"
        val result = parser.parse("HBL", body, ts)
        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(BigDecimal("10000"), result.amount)
    }

    @Test fun `parse transfer SMS extracts counterparty name`() {
        val body = "Amount Rs.5,000 transferred to Sara Ahmed (03111234567). Balance: Rs.20,000."
        val result = parser.parse("HBL", body, ts)
        assertEquals("Sara Ahmed", result.counterparty)
    }

    // ── BILL_PAYMENT ──────────────────────────────────────────────────────────

    @Test fun `parse bill payment SMS returns BILL_PAYMENT type`() {
        val body = "Bill payment of Rs.3,500 has been processed. Available Balance: Rs.16,500."
        val result = parser.parse("HBL", body, ts)
        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("3500"), result.amount)
    }

    // ── ATM WITHDRAWAL ────────────────────────────────────────────────────────

    @Test fun `parse ATM withdrawal SMS returns DEBIT with ATM counterparty`() {
        val body = "Withdrawal of Rs.10,000 at ATM 001. Available Balance: Rs.30,000."
        val result = parser.parse("HBL", body, ts)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("10000"), result.amount)
        assertEquals("ATM", result.counterparty)
    }

    // ── UNKNOWN / FALLBACK ────────────────────────────────────────────────────

    @Test fun `parse unrecognised SMS returns UNKNOWN with low confidence`() {
        val body = "Dear HBL customer, please update your KYC."
        val result = parser.parse("HBL", body, ts)
        assertEquals(TransactionType.UNKNOWN, result.type)
        assertTrue(result.confidenceScore < 0.5f)
    }

    // ── IDEMPOTENCY ───────────────────────────────────────────────────────────

    @Test fun `same SMS produces same deterministic ID`() {
        val body = "Rs.5,000 has been debited from your HBL account. Available Balance: Rs.45,000."
        val id1 = parser.parse("HBL", body, ts).id
        val id2 = parser.parse("HBL", body, ts).id
        assertEquals(id1, id2)
    }

    @Test fun `different timestamp produces different ID`() {
        val body = "Rs.5,000 has been debited from your HBL account. Available Balance: Rs.45,000."
        val id1 = parser.parse("HBL", body, ts).id
        val id2 = parser.parse("HBL", body, ts + 1000).id
        assertNotEquals(id1, id2)
    }

    // ── EDGE CASES ────────────────────────────────────────────────────────────

    @Test fun `parse handles empty body gracefully`() {
        val result = parser.parse("HBL", "", ts)
        assertEquals(TransactionType.UNKNOWN, result.type)
    }

    @Test fun `currency is always PKR`() {
        val body = "Rs.5,000 has been debited from your HBL account. Available Balance: Rs.45,000."
        assertEquals("PKR", parser.parse("HBL", body, ts).currency)
    }
}
