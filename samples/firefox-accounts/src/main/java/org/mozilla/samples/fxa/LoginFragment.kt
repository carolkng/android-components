/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.samples.fxa

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment

open class LoginFragment : Fragment() {

    protected lateinit var authUrl: String
    protected lateinit var redirectUrl: String
    protected var listener: OnLoginCompleteListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            authUrl = it.getString(AUTH_URL)
            redirectUrl = it.getString(REDIRECT_URL)
        }
    }

    @Suppress("TooGenericExceptionThrown")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginCompleteListener) {
            listener = context
        } else {
            throw IllegalStateException(context.toString() + " must implement OnLoginCompleteListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnLoginCompleteListener {
        fun onLoginComplete(code: String, state: String, fragment: LoginFragment)
    }

    companion object {
        const val AUTH_URL = "authUrl"
        const val REDIRECT_URL = "redirectUrl"
    }
}
