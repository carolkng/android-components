/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.samples.fxa

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.customtabs.CustomTabsIntent
import android.view.View
import android.content.Intent
import android.widget.TextView
import mozilla.components.service.fxa.Config
import mozilla.components.service.fxa.FirefoxAccount
import mozilla.components.service.fxa.FxaClient
import mozilla.components.service.fxa.FxaResult
import mozilla.components.service.fxa.Profile

open class MainActivity : AppCompatActivity() {

    private var account: FirefoxAccount? = null
    private var scopes: Array<String> = arrayOf("profile")

    companion object {
        const val CLIENT_ID = "12cc4070a481bc73"
        const val REDIRECT_URL = "fxaclient://android.redirect"
        const val CONFIG_URL = "https://latest.dev.lcip.org"

        init {
            FxaClient.init()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Config.custom(CONFIG_URL).then(object : FxaResult.OnValueListener<Config, FirefoxAccount> {
            override fun onValue(value: Config?): FxaResult<FirefoxAccount>? {
                if (value != null) {
                    return FxaResult.fromValue(FirefoxAccount(value, CLIENT_ID))
                }
                return null
            }
        }, null).then(object : FxaResult.OnValueListener<FirefoxAccount, Void> {
            override fun onValue(value: FirefoxAccount?): FxaResult<Void>? {
                if (value != null) {
                    account = value
                    val btn = findViewById<View>(R.id.button)
                    btn.setOnClickListener {
                        openOAuthTab()
                    }
                }
                return null
            }
        }, null)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action
        val data = intent.dataString

        if (Intent.ACTION_VIEW == action && data != null) {
            val txtView: TextView = findViewById(R.id.txtView)
            val url = Uri.parse(data)
            val code = url.getQueryParameter("code")
            val state = url.getQueryParameter("state")

            account?.completeOAuthFlow(code, state)

            account?.getProfile()!!.then(object : FxaResult.OnValueListener<Profile, Void> {
                override fun onValue(value: Profile?): FxaResult<Void>? {
                    if (value != null) {
                        txtView.text = "${value.displayName ?: ""} ${value.email}"
                    }
                    return null
                }
            }, null)
        }
    }

    private fun openTab(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
                .addDefaultShareMenuItem()
                .setShowTitle(true)
                .build()

        customTabsIntent.intent.data = Uri.parse(url)
        customTabsIntent.launchUrl(this@MainActivity, Uri.parse(url))
    }

    private fun openOAuthTab() {
        account?.beginOAuthFlow(REDIRECT_URL, scopes, false)!!.then(object : FxaResult.OnValueListener<String?, Void> {
            override fun onValue(value: String?): FxaResult<Void>? {
                if (value != null) {
                    openTab(value)
                }
                return null
            }
        }, null)
    }
}
