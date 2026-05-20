package com.hisaab.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.hisaab.parser.SmsParserRegistry

/**
 * HisaabNotificationService — captures push notifications from financial apps (PRD F1).
 *
 * Monitored packages: JazzCash, Easypaisa, NayaPay, SadaPay, Zindigi, UPaisa.
 * When a notification from one of these arrives, the body text is fed to SmsParserRegistry
 * as if it were an SMS. Tier 3 LLM fallback handles edge cases.
 *
 * Enable via: Settings → Apps → Special access → Notification access → Hisaab
 */
class HisaabNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg !in MONITORED_PACKAGES) return

        val extras = sbn.notification.extras ?: return
        val title  = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text   = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()  ?: ""

        val body = "$title $text".trim()
        if (body.isBlank()) return

        val sender    = PACKAGE_TO_SENDER[pkg] ?: pkg
        val timestamp = sbn.postTime

        val parsed = SmsParserRegistry.parse(body, sender, timestamp)
        if (parsed != null) {
            SmsIngestionWorker.enqueue(applicationContext, parsed)
        }
    }

    companion object {
        private val MONITORED_PACKAGES = setOf(
            "com.jazz.jazzcash",
            "com.telenor.easypaisa",
            "com.nayapay.app",
            "com.sadapay.sadapay",
            "com.zindigi.banking",
            "pk.digitalpak.upaisa",
        )
        private val PACKAGE_TO_SENDER = mapOf(
            "com.jazz.jazzcash"       to "JazzCash",
            "com.telenor.easypaisa"   to "Easypaisa",
            "com.nayapay.app"         to "NayaPay",
            "com.sadapay.sadapay"     to "SadaPay",
            "com.zindigi.banking"     to "Zindigi",
            "pk.digitalpak.upaisa"    to "UPaisa",
        )
    }
}
