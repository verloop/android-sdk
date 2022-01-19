package io.verloop

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import io.verloop.sdk.model.LogoutRequestBody
import io.verloop.sdk.service.LogoutWorker
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutWorkManagerTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    @Throws(Exception::class)
    fun testLogoutWorker() {

        val data = Data.Builder()
            .putString(LogoutRequestBody.CLIENT_ID, "hello")
            .putString(LogoutRequestBody.USER_ID, "xyz")
            .build()

        // Create request
        val request = OneTimeWorkRequestBuilder<LogoutWorker>()
            .setInputData(data)
            .build()

        val workManager = WorkManager.getInstance(context)
        // Enqueue and wait for result. This also runs the Worker synchronously
        // because we are using a SynchronousExecutor.
        workManager.enqueue(request).result.get()

        // Get WorkInfo and outputData
        val workInfo = workManager.getWorkInfoById(request.id).get()

        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }
}