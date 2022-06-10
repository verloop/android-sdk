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


}