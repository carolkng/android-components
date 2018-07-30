/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.samples.fxa

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewLoginFragment : LoginFragment() {
    private var mWebView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_view, container, false)
        val webView = view.findViewById<WebView>(R.id.webview)
        // Need JS, cookies and localStorage.
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url != null && url.startsWith(redirectUrl)) {
                    val uri = Uri.parse(url)
                    val code = uri.getQueryParameter("code")
                    val state = uri.getQueryParameter("state")
                    if (code != null && state != null) {
                        listener?.onLoginComplete(code, state, this@WebViewLoginFragment)
                    }
                }

                super.onPageStarted(view, url, favicon)
            }
        }
        webView.loadUrl(authUrl)

        mWebView?.destroy()
        mWebView = webView

        return view
    }

    override fun onPause() {
        super.onPause()
        mWebView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mWebView?.onResume()
    }

    companion object {
        fun create(authUrl: String, redirectUrl: String): WebViewLoginFragment =
                WebViewLoginFragment().apply {
                    arguments = Bundle().apply {
                        putString(AUTH_URL, authUrl)
                        putString(REDIRECT_URL, redirectUrl)
                    }
                }
    }
}