package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import androidx.work.workDataOf

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        makeStatusNotification(
            context.resources.getString(R.string.blurring_image),
            context
        )

        return withContext(Dispatchers.Default) {

            // This is an utility function added to emulate slower work.
            delay(DELAY_TIME_MILLIS)

            return@withContext try {
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage = context.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                    errorMessage
                }

                val resolver = context.contentResolver

                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(resourceUri.toUri())
                )

                val output = blurBitmap(picture, blurLevel)

                val outputUri = writeBitmapToFile(context, output)

                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

                Result.success(outputData)
            } catch (throwable: Throwable) {
                Log.e(TAG, context.getString(R.string.error_applying_blur), throwable)
                Result.failure()
            }
        }
    }

}