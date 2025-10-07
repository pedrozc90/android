/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluromatic.data

import android.content.Context
import android.net.Uri
import androidx.compose.ui.unit.Constraints
import androidx.lifecycle.asFlow
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.multiprocess.IWorkManagerImpl
import com.example.bluromatic.IMAGE_MANIPULATION_WORK_NAME
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.TAG_OUTPUT
import com.example.bluromatic.getImageUri
import com.example.bluromatic.workers.BlurWorker
import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.internal.throwMissingFieldException

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    private var imageUri: Uri = context.getImageUri()
    private val workManager = WorkManager.getInstance(context)

    override val outputWorkInfo: Flow<WorkInfo> = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
        .asFlow()
        .mapNotNull { if (it.isNotEmpty()) it.first() else null }

    /**
     * Create the WorkRequests to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    override fun applyBlur(blurLevel: Int) {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        // Add WorkRequest to Cleanup temporary images
        val cleanup = OneTimeWorkRequest.from(CleanupWorker::class.java)

        var continuation = workManager.beginUniqueWork(
            IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            cleanup
        )

        // create WorkRequest to blur the image
        val builder = OneTimeWorkRequestBuilder<BlurWorker>()

        // New code for input data object
        builder.setInputData(createInputDataForWorkRequest(blurLevel, imageUri))
        builder.setConstraints(constraints)

        // start the work
        // workManager.enqueue(builder.build())
        continuation = continuation.then(builder.build())

        // Add WorkRequest to save the image to the filesystem
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .addTag(TAG_OUTPUT)
            .build()

        continuation = continuation.then(save)

        // Start the work
        continuation.enqueue()
    }

//    fun _applyBlur(blurLevel: Int) {
//        // Add WorkRequest to Cleanup temporary images
//        val cleanup = OneTimeWorkRequest.from(CleanupWorker::class.java)
//
//        // create WorkRequest to blur the image
//        val blur = OneTimeWorkRequestBuilder<BlurWorker>()
//            .setInputData(createInputDataForWorkRequest(blurLevel, imageUri))
//            .build()
//
//        // Add WorkRequest to save the image to the filesystem
//        val save = OneTimeWorkRequest.from(SaveImageToFileWorker::class.java)
//
//        workManager.beginWith(cleanup)
//            .then(blur)
//            .then(save)
//            .enqueue()
//    }

    /**
     * Cancel any ongoing WorkRequests
     * */
    override fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    /**
     * Creates the input data bundle which includes the blur level to
     * update the amount of blur to be applied and the Uri to operate on
     * @return Data which contains the Image Uri as a String and blur level as an Integer
     */
    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        val builder = Data.Builder()
            .putString(KEY_IMAGE_URI, imageUri.toString())
            .putInt(KEY_BLUR_LEVEL, blurLevel)
        return builder.build()
    }



}
