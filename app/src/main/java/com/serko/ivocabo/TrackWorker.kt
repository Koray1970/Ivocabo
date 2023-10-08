package com.serko.ivocabo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TrackWorker(context: Context, parameters:WorkerParameters): CoroutineWorker(context,parameters) {
    override suspend fun doWork(): Result {

        return Result.success()
    }
}