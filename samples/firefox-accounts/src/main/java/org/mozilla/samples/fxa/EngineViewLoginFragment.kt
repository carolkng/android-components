/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.samples.fxa

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mozilla.components.browser.engine.system.SystemEngine
import mozilla.components.concept.engine.EngineView

class EngineViewLoginFragment : LoginFragment() {
    private var mEngineView: EngineView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_engine_view_login, container, false)
        val engine: EngineView = SystemEngine().createView(this.context!!, attrs = null)
        return view
    }

    companion object {
        fun create(authUrl: String, redirectUrl: String): EngineViewLoginFragment =
                EngineViewLoginFragment().apply {
                    arguments = Bundle().apply {
                        putString(AUTH_URL, authUrl)
                        putString(REDIRECT_URL, redirectUrl)
                    }
                }
    }
}