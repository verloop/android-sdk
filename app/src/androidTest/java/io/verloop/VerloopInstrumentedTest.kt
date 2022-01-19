package io.verloop

import android.content.Context
import android.content.Intent
import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.verloop.sdk.VerloopConfig
import io.verloop.sdk.ui.VerloopActivity
import org.junit.Assert

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class VerloopInstrumentedTest {

    @Test
    fun useAppContext() {
        var context:Context = Mockito.mock(Context::class.java)
        val i = Intent(context, VerloopActivity::class.java)
        assertNotNull(i)
    }

    @Test
    fun testVerloopConfigParcelable() {
        val config = VerloopConfig.Builder().clientId("hello").userId("xyx").build()
        // Obtain a Parcel object and write the parcelable object to it:
        val parcel = Parcel.obtain()
        config.writeToParcel(parcel, 0)

        // After you're done with writing, you need to reset the parcel for reading:
        parcel.setDataPosition(0)

        // Reconstruct object from parcel and asserts:
        val createdFromParcel: VerloopConfig = VerloopConfig.CREATOR.createFromParcel(parcel)
        Assert.assertEquals(config, createdFromParcel)
    }
}