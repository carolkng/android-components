package org.mozilla.samples.fxa

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient

private const val AUTH_URL = "authUrl"
private const val REDIRECT_URL = "redirectUrl"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ViewFragment.OnLoginCompleteListener] interface
 * to handle interaction events.
 * Use the [ViewFragment.create] factory method to
 * create an instance of this fragment.
 *
 */
class ViewFragment : Fragment() {
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
        val v: View = inflater.inflate(R.layout.fragment_view, container, false)
        val wv: WebView = v.findViewById(R.id.webview)
        // Need JS, cookies and localStorage.
        wv.settings.domStorageEnabled = true
        wv.settings.javaScriptEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)

        wv.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url != null && url.startsWith(redirectUrl)) {
                    val uri = Uri.parse(url)
                    val code = uri.getQueryParameter("code")
                    val state = uri.getQueryParameter("state")
                    if (code != null && state != null) {
                        listener?.onLoginComplete(code, state, this@ViewFragment)
                    }
                }

                super.onPageStarted(view, url, favicon)
            }
        }
        wv.loadUrl(authUrl)

        return v
    }

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnLoginCompleteListener {
        fun onLoginComplete(code: String, state: String, fragment: ViewFragment)
    }

    companion object {
        fun create(authUrl: String, redirectUrl: String): ViewFragment =
                ViewFragment().apply {
                    this.authUrl = authUrl
                    this.redirectUrl = redirectUrl
                }
    }
}
