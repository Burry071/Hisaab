---
name: hisaab-parser
description: Use when writing or extending Pakistani bank/wallet SMS parsers. 
  Ensures new parsers follow BankParser base class and match existing patterns.
---

Always extend BankParser. Implement: getBankName(), getCurrency() = "PKR",
canHandle(sender), isTransactionMessage(), extractAmount(), 
extractTransactionType(), extractMerchant(), extractAccountLast4().

Reference FaysalBankParser.kt as the canonical example.
Every new parser needs a corresponding test file in parser-core/src/test/.
Use PKR not INR. Add sender shortcodes to canHandle().
