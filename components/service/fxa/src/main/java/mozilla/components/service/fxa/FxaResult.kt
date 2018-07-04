package mozilla.components.service.fxa

import org.jetbrains.annotations.Nullable
import java.util.ArrayList

/**
 * FxaResult is a class that represents an asynchronous result.
 *
 * @param <T> The type of the value delivered via the FxaResult.
 */
class FxaResult<T>() {

    private var mComplete: Boolean = false
    private var mValue: T? = null
    private var mError: Exception? = null

    private val mListeners: ArrayList<Listener<T>> = ArrayList()

    private interface Listener<T> {
        fun onValue(value: T?)

        fun onException(exception: Exception?)
    }

    /**
     * This constructs a result from another result. All state except listeners is copied.
     *
     * @param from The [FxaResult] to copy.
     */
    constructor(from: FxaResult<T>) : this() {
        mComplete = from.mComplete
        mValue = from.mValue
        mError = from.mError
    }

    override fun equals(other: Any?): Boolean {
        if (other is FxaResult<*>) {
            val result = other as FxaResult<*>?
            return result!!.mComplete == mComplete &&
                    result.mError === mError &&
                    result.mValue === mValue
        }

        return false
    }

    /**
     * Completes this result based on another result.
     *
     * @param other The result that this result should mirror
     */
    private fun completeFrom(other: FxaResult<T>?) {
        if (other == null) {
            complete(null)
            return
        }

        other.then(object : OnValueListener<T, Void> {
            override fun onValue(value: T?): FxaResult<Void>? {
                complete(value)
                return null
            }
        }, object : OnExceptionListener<Void> {
            override fun onException(exception: Exception?): FxaResult<Void>? {
                completeExceptionally(exception)
                return null
            }
        })
    }

    /**
     * Adds listeners to be called when the [GeckoResult] is completed either with
     * a value or [Exception]. Listeners will be invoked on the same thread in which the
     * [GeckoResult] was completed.
     *
     * @param valueListener An instance of [OnValueListener], called when the
     * [GeckoResult] is completed with a value.
     * @param exceptionListener An instance of [OnExceptionListener], called when the
     * [GeckoResult] is completed with an [Exception].
     */
    @Synchronized
    fun <U> then(@Nullable valueListener: OnValueListener<T, U>?, @Nullable exceptionListener: OnExceptionListener<U>?): FxaResult<U> {
        if (valueListener == null && exceptionListener == null) {
            throw IllegalArgumentException("At least one listener should be non-null")
        }

        val result = FxaResult<U>()
        val listener = object : Listener<T> {
            override fun onValue(value: T?) {
                if (valueListener == null) {
                    return
                }

                result.completeFrom(valueListener.onValue(value))
            }

            override fun onException(exception: Exception?) {
                if (exceptionListener == null) {
                    return
                }

                result.completeFrom(exceptionListener.onException(exception))
            }
        }

        if (haveValue()) {
            listener.onValue(mValue)
        } else if (haveError()) {
            listener.onException(mError)
        } else {
            mListeners.add(listener)
        }

        return result
    }

    /**
     * This completes the result with the specified value. IllegalStateException is thrown
     * if the result is already complete.
     *
     * @param value The value used to complete the result.
     * @throws IllegalStateException
     */
    @Synchronized
    protected fun complete(value: T?) {
        if (mComplete) {
            throw IllegalStateException("result is already complete")
        }

        mValue = value
        mComplete = true

        ArrayList(mListeners).forEach { it.onValue(mValue) }
    }

    /**
     * This completes the result with the specified [Exception]. IllegalStateException is thrown
     * if the result is already complete.
     *
     * @param exception The [Exception] used to complete the result.
     * @throws IllegalStateException
     */
    @Synchronized
    protected fun completeExceptionally(exception: Exception?) {
        if (mComplete) {
            throw IllegalStateException("result is already complete")
        }

        if (exception == null) {
            throw IllegalArgumentException("Exception must not be null")
        }

        mError = exception
        mComplete = true

        ArrayList(mListeners).forEach { it.onException(mError) }
    }

    /**
     * An interface used to deliver values to listeners of a [FxaResult]
     *
     * @param <T> This is the type of the value delivered via [.onValue]
     * @param <U> This is the type of the value for the result returned from [.onValue]
     */
    interface OnValueListener<T, U> {
        /**
         * Called when a [FxaResult] is completed with a value. This will be
         * called on the same thread in which the result was completed.
         *
         * @param value The value of the [FxaResult]
         * @return A new [FxaResult], used for chaining results together.
         * May be null.
         */
        fun onValue(value: T?): FxaResult<U>?
    }

    /**
     * An interface used to deliver exceptions to listeners of a [FxaResult]
     *
     * @param <V> This is the type of the vale for the result returned from [.onException]
     */
    interface OnExceptionListener<V> {
        fun onException(exception: Exception?): FxaResult<V>?
    }

    private fun haveValue(): Boolean {
        return mComplete && mError == null
    }

    private fun haveError(): Boolean {
        return mComplete && mError != null
    }

    companion object {
        private val LOGTAG = "FxaResult"

        /**
         * This constructs a result that is fulfilled with the specified value.
         *
         * @param value The value used to complete the newly created result.
         * @return The completed [FxaResult]
         */
        fun <U> fromValue(value: U): FxaResult<U> {
            val result = FxaResult<U>()
            result.complete(value)
            return result
        }

        /**
         * This constructs a result that is completed with the specified [Exception].
         * May not be null.
         *
         * @param exception The exception used to complete the newly created result.
         * @return The completed [FxaResult]
         */
        fun <T> fromException(exception: Exception): FxaResult<T> {
            val result = FxaResult<T>()
            result.completeExceptionally(exception)
            return result
        }
    }
}