package org.mozilla.samples.fxa

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

private const val AUTH_URL = "authUrl"
private const val REDIRECT_URL = "redirectUrl"

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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_engine_view_login, container, false)
        val wv: WebView = v.findViewById(R.id.engineview)

        return v
    }

    @Suppress("TooGenericExceptionThrown")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginCompleteListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnLoginCompleteListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnLoginCompleteListener {
        fun onLoginComplete(code: String, state: String, fragment: EngineViewLoginFragment)
    }

    companion object {
        fun create(authUrl: String, redirectUrl: String): EngineViewLoginFragment =
                EngineViewLoginFragment().apply {
                    this.authUrl = authUrl
                    this.redirectUrl = redirectUrl
                }
    }
}
