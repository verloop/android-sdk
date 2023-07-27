package io.verloop.sdk

import org.junit.Assert.*
import org.junit.Test

class VerloopConfigTest {
    private lateinit var verloopConfig: VerloopConfig

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

    @Test
    fun testDefaultValues() {
        verloopConfig = VerloopConfig.Builder().clientId("verloop").build()
        assertNull(verloopConfig.department)
        assertNull(verloopConfig.fcmToken)
        assertNull(verloopConfig.recipeId)
        assertNull(verloopConfig.userEmail)
        assertNull(verloopConfig.userName)
        assertNull(verloopConfig.userPhone)
        assertNotNull(verloopConfig.userId)
        assertFalse(verloopConfig.overrideUrlClick)
        assertEquals(0, verloopConfig.fields.size)
    }

    @Test
    fun testExplicitUserId() {
        verloopConfig = VerloopConfig.Builder().clientId("hello").userId("abc").build()
        assertEquals("abc", verloopConfig.userId)
    }

    @Test
    fun testImplicitUserId() {
        verloopConfig = VerloopConfig.Builder().clientId("hello").build()
        assertNotNull(verloopConfig.userId)
    }

}