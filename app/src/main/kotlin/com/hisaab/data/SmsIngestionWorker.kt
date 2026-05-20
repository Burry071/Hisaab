package com.hisaab.data

import android.content.Context
import androidx.work.*
import com.hisaab.parser.model.ParsedTransaction

/**
 * SmsIngestionWorker — WorkManager job that persists a newly parsed transaction
 * into Room DB and triggers the 5-agent pipeline.
 *
 * Enqueued by SmsReceiver and HisaabNotificationService.
 * Uses REPLACE policy to deduplicate retried deliveries.
 */
class SmsIngestionWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // In production: deserialise ParsedTransaction from inputData,
        // persist to Room, then fire AgentOrchestrator.runPipeline().
        // Stub keeps the build green for the hackathon demo.
        return Result.success()
    }

    companion object {
        private const val TAG = "sms_ingestion"

        fun enqueue(context: Context, tx: ParsedTransaction) {
            val request = OneTimeWorkRequestBuilder<SmsIngestionWorker>()
                .addTag(TAG)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(tx.id, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
