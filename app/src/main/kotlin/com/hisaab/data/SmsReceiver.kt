package com.hisaab.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.hisaab.parser.SmsParserRegistry

/**
 * SmsReceiver — listens for new incoming SMS and routes to parser pipeline.
 *
 * Fires IngestionAgent via WorkManager when a matching financial SMS arrives.
 * Sends a local broadcast ("com.hisaab.NEW_SMS") picked up by AgentViewModel.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val body      = sms.messageBody   ?: continue
            val sender    = sms.originatingAddress ?: "UNKNOWN"
            val timestamp = sms.timestampMillis

            val parsed = SmsParserRegistry.parse(body, sender, timestamp)
            if (parsed != null) {
                // Enqueue WorkManager job to run IngestionAgent on new transaction
                SmsIngestionWorker.enqueue(context, parsed)
            }
        }
    }
}
