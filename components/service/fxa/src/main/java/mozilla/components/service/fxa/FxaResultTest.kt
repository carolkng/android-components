package mozilla.components.service.fxa

import android.content.Intent
import android.os.Parcelable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.util.HashSet
import kotlin.collections.ArrayList

@RunWith(RobolectricTestRunner::class)
class FxaResultTest {

    @Test
    fun thenWithResult() {
        FxaResult.fromValue(42).then(object : FxaResult.OnValueListener<Integer, Void> {
            override fun onValue(value: Integer?) FxaResult<Void>? {
                assertEquals(value, 42)
                return null
            }
        }, null)
    }

    @Test
    fun thenWithException() {
        FxaResult.fromException(Exception("42")).then(null, object: FxaResult.OnExceptionListener<Integer, Void> {
            override fun onException(exception: Exception?): FxaResult<Void>? {
                assertEquals(exception.message, "42")
                return null
            }
        })
    }

    @Test(expected = IllegalArgumentException::class)
    fun thenNoListeners() {
        FxaResult.fromValue(42).then(null, null)
    }

    @Test
    fun resultChaining() {
        FxaResult.fromValue(42).then(object: FxaResult.OnValueListener<Integer, String> {
            override fun onValue(value: Integer?) FxaResult<String>? {
                assertEquals(value, 42)
                return FxaResult.fromValue("string")
            }
        }, null).then(object: FxaResult.OnValueListener<String, Integer> {
            override fun onValue(value: String?) FxaResult<Integer>? {
                assertEquals(value, "string")
                throw Exception("exception message")
                return FxaResult.fromValue(42)
            }
        }, null).then(null, object: FxaResult.OnExceptionListener<String> {
            override fun onException(exception: Exception?): FxaResult<String>? {
                assertEquals(exception.message, "exception message")
                return null
            }
        })
    }
}
