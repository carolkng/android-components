/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.service.fxa

import android.content.Context
import android.net.Uri
import android.webkit.WebView

class FirefoxAccount(override var rawPointer: RawFxAccount?) : RustObject<RawFxAccount>() {

    constructor(config: Config, clientId: String, redirectUri: String): this(null) {
        this.rawPointer = safeSync { e ->
            FxaClient.INSTANCE.fxa_new(config.consumePointer(), clientId, redirectUri, e)
        }
    }

    override fun destroy(p: RawFxAccount) {
        safeSync { FxaClient.INSTANCE.fxa_free(p) }
    }

    fun beginOAuthFlow(scopes: Array<String>, wantsKeys: Boolean): FxaResult<String> {
        return safeAsync { e ->
            val scope = scopes.joinToString(" ")
            val p = FxaClient.INSTANCE.fxa_begin_oauth_flow(validPointer(), scope, wantsKeys, e)
            getAndConsumeString(p) ?: ""
        }
    }

    // NOTE: For this to ever work properly, the client has to manually set an intent filter with
    //       the redirectUri anyways, so it's not really worth pursuing. If we want to provide a
    //       real convenience method, I'd prefer something that doesn't require a bunch of extra
    //       work in the middle
//    fun openCustomOAuthTab(redirectURI: String, scopes: Array<String>, wantsKeys: Boolean, context: Context) {
//        val openTab = { value: String? ->
//            val customTabsIntent = CustomTabsIntent.Builder()
//                    .addDefaultShareMenuItem()
//                    .setShowTitle(true)
//                    .build()
//
//            customTabsIntent.intent.data = Uri.parse(value)
//            customTabsIntent.launchUrl(context, Uri.parse(value))
//            FxaResult<Void>()
//        }
//        this.beginOAuthFlow(scopes, wantsKeys).then(openTab)
//    }

    /**
     * Takes a WebView container and loads a WebView with the intent of completing the OAuth flow,
     * and automatically mutates the FxA state.
     */
    fun webviewOAuthFlow(
            redirectUri: String,
            scopes: Array<String>,
            wantsKeys: Boolean,
            webview: WebView) {
        val openView = {

        }
        this.beginOAuthFlow(scopes, wantsKeys).then(openView)
    }
    fun getProfile(ignoreCache: Boolean): FxaResult<Profile> {
        return safeAsync { e ->
            val p = FxaClient.INSTANCE.fxa_profile(validPointer(), ignoreCache, e)
            Profile(p)
        }
    }

    fun getProfile(): FxaResult<Profile> {
        return getProfile(false)
    }

    fun newAssertion(audience: String): String? {
        return safeSync { e ->
            val p = FxaClient.INSTANCE.fxa_assertion_new(this.validPointer(), audience, e)
            getAndConsumeString(p)
        }
    }

    fun getTokenServerEndpointURL(): String? {
        return safeSync { e ->
            val p = FxaClient.INSTANCE.fxa_get_token_server_endpoint_url(validPointer(), e)
            getAndConsumeString(p)
        }
    }

    fun getSyncKeys(): SyncKeys {
        return safeSync { e ->
            val p = FxaClient.INSTANCE.fxa_get_sync_keys(validPointer(), e)
            SyncKeys(p)
        }
    }

    fun completeOAuthFlow(code: String, state: String): FxaResult<OAuthInfo> {
        return safeAsync { e ->
            val p = FxaClient.INSTANCE.fxa_complete_oauth_flow(validPointer(), code, state, e)
            OAuthInfo(p)
        }
    }

    fun getOAuthToken(scopes: Array<String>): FxaResult<OAuthInfo> {
        return safeAsync { e ->
            val scope = scopes.joinToString(" ")
            val p = FxaClient.INSTANCE.fxa_get_oauth_token(validPointer(), scope, e)
                    ?: throw FxaException.Unauthorized("Restart OAuth flow")
            OAuthInfo(p)
        }
    }

    fun toJSONString(): String? {
        return safeSync { e ->
            val p = FxaClient.INSTANCE.fxa_to_json(validPointer(), e)
            getAndConsumeString(p)
        }
    }

    companion object {
        fun from(
            config: Config,
            clientId: String,
            redirectUri: String,
            webChannelResponse: String
        ): FxaResult<FirefoxAccount> {
            return RustObject.safeAsync { e ->
                val p = FxaClient.INSTANCE.fxa_from_credentials(config.consumePointer(),
                        clientId, redirectUri, webChannelResponse, e)
                FirefoxAccount(p)
            }
        }

        fun fromJSONString(json: String): FxaResult<FirefoxAccount> {
            return RustObject.safeAsync { e ->
                val p = FxaClient.INSTANCE.fxa_from_json(json, e)
                FirefoxAccount(p)
            }
        }
    }
}
