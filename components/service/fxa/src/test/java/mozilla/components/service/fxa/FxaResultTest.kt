package mozilla.components.service.fxa

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FxaResultTest {

    @Test
    fun thenWithResult() {
        FxaResult.fromValue(42).then { value: Int? ->
            assertEquals(value, 42)
            FxaResult<Void>()
        }
    }

    @Test
    fun thenWithException() {
        FxaResult.fromException<Void>(Exception("exception message")).then { value: Exception ->
            assertEquals(exception.message, "exception message")
            FxaResult<Void>()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun thenNoListeners() {
        FxaResult.fromValue(42).then(null as FxaResult.OnValueListener<Int, Void>, null)
    }

    @Test
    fun resultChaining() {
        FxaResult.fromValue(42).then { value: Int? ->
            assertEquals(value, 42)
            FxaResult.fromValue("string")
        }.then { value: String? ->
            assertEquals(value, "String")
            throw Exception("exception message")
            FxaResult.fromValue(42)
        }.then { value: Exception ->
            assertEquals(exception.message, "exception message")
        }
    }
}
