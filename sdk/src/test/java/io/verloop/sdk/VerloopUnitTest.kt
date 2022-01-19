package io.verloop.sdk

import io.verloop.sdk.utils.CommonUtils
import org.junit.Assert
import org.junit.Test

class VerloopUnitTest {

    @Test
    fun colorHexConversion() {
        val color = CommonUtils.getExpandedColorHex("#4FD")
        Assert.assertEquals("#44FFDD", color)
    }

    @Test(expected = VerloopException::class)
    fun invalidVerloopConfig() {
        VerloopConfig.Builder().userId("Verloop").build()
    }

    @Test
    fun validVerloopConfig() {
        try {
            VerloopConfig.Builder().clientId("Verloop").build()
            assert(true)
        } catch (e: VerloopException) {
            assert(false)
        }
    }
}