package mozilla.components.service.fxa

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FxaResultTest {

    @Test
    fun thenWithResult() {
        FxaResult.fromValue(42).then(object : FxaResult.OnValueListener<Int, Void> {
            override fun onValue(value: Int?): FxaResult<Void>? {
                assertEquals(value, 42)
                return null
            }
        }, null)
    }

    @Test
    fun thenWithException() {
        FxaResult.fromException<String>(Exception("42")).then(null, object: FxaResult.OnExceptionListener<Void> {
            override fun onException(exception: Exception): FxaResult<Void>? {
                assertEquals(exception.message, "42")
                return null
            }
        })
    }

    @Test(expected = IllegalArgumentException::class)
    fun thenNoListeners() {
        FxaResult.fromValue(42).then(null as FxaResult.OnValueListener<Int, Void>, null)
    }

    @Test
    fun resultChaining() {
        FxaResult.fromValue(42).then(object: FxaResult.OnValueListener<Int, String> {
            override fun onValue(value: Int?): FxaResult<String>? {
                assertEquals(value, 42)
                return FxaResult.fromValue("string")
            }
        }, null).then(object: FxaResult.OnValueListener<String, Int> {
            override fun onValue(value: String?): FxaResult<Int>? {
                assertEquals(value, "string")
                throw Exception("exception message")
                return FxaResult.fromValue(42)
            }
        }, null).then(null, object: FxaResult.OnExceptionListener<String> {
            override fun onException(exception: Exception): FxaResult<String>? {
                assertEquals(exception.message, "exception message")
                return null
            }
        })
    }
}
