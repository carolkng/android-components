/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.samples.fxa

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_engine_view_login.*
import mozilla.components.browser.engine.system.SystemEngine
import mozilla.components.browser.engine.system.SystemEngineSession
import mozilla.components.browser.engine.system.SystemEngineView
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView

class EngineViewLoginFragment : Fragment() {

    private lateinit var authUrl: String
    private lateinit var redirectUrl: String
    private var listener: OnLoginCompleteListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            authUrl = it.getString(AUTH_URL)
            redirectUrl = it.getString(REDIRECT_URL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_engine_view_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SystemEngineSession()
        session.register(object : EngineSession.Observer {
            override fun onLocationChange(url: String) {
                Log.e("onLoc", url)
                if (url.startsWith(redirectUrl)) {
                    val uri = Uri.parse(url)
                    val code = uri.getQueryParameter("code")
                    val state = uri.getQueryParameter("state")
                    if (code != null && state != null) {
                        listener?.onLoginComplete(code, state, this@EngineViewLoginFragment)
                    }
                }
                super.onLocationChange(url)
            }
        })
        session.loadUrl(authUrl)
        engineView.render(session)
    }

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
        fun onLoginComplete(code: String, state: String, fragment: Fragment)
    }

    companion object {
        const val AUTH_URL = "authUrl"
        const val REDIRECT_URL = "redirectUrl"

        fun create(authUrl: String, redirectUrl: String): EngineViewLoginFragment =
                EngineViewLoginFragment().apply {
                    arguments = Bundle().apply {
                        putString(AUTH_URL, authUrl)
                        putString(REDIRECT_URL, redirectUrl)
                    }
                }
    }
}
