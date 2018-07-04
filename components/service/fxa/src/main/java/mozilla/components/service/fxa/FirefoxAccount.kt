/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.service.fxa

class FirefoxAccount(override var rawPointer: FxaClient.RawFxAccount?) : RustObject<FxaClient.RawFxAccount>() {

    constructor(config: Config, clientId: String): this(null) {
        val e = Error.ByReference()
        val result = FxaClient.INSTANCE.fxa_new(config.consumePointer(), clientId, e)
        if (e.isFailure()) throw FxaException.fromConsuming(e)!!
        this.rawPointer = result
    }

    override fun destroyPointer(p: FxaClient.RawFxAccount) {
        FxaClient.INSTANCE.fxa_free(p)
    }

    fun beginOAuthFlow(redirectURI: String, scopes: Array<String>, wantsKeys: Boolean): String? {
        val scope = scopes.joinToString(" ")
        val e = Error.ByReference()
        val p = FxaClient.INSTANCE.fxa_begin_oauth_flow(this.validPointer(), redirectURI, scope, wantsKeys, e)
        if (e.isFailure()) {
            return FxaResult.fromException(FxaException.fromConsuming(e)!!)
        } else {
            return FxaResult.fromValue(getAndConsumeString(p))
        }
    }

    fun getProfile(ignoreCache: Boolean): FxaResult<Profile> {
        val e = Error.ByReference()
        val p = FxaClient.INSTANCE.fxa_profile(this.validPointer(), ignoreCache, e)
        if (e.isFailure()) {
            return FxaResult.fromException(FxaException.fromConsuming(e)!!)
        } else {
            return FxaResult.fromValue(Profile(p))
        }
    }

    fun getProfile(): FxaResult<Profile> {
        return getProfile(false)
    }

    fun newAssertion(audience: String): String? {
        val e = Error.ByReference()
        val p = FxaClient.INSTANCE.fxa_assertion_new(this.validPointer(), audience, e)
        if (e.isFailure()) throw FxaException.fromConsuming(e)!!
        return getAndConsumeString(p)
    }

    fun getTokenServerEndpointURL(): String? {
        val e = Error.ByReference()
        val p = FxaClient.INSTANCE.fxa_get_token_server_endpoint_url(this.validPointer(), e)
        if (e.isFailure()) throw FxaException.fromConsuming(e)!!
        return getAndConsumeString(p)
    }

    fun getSyncKeys(): SyncKeys {
        val e = Error.ByReference()
        val p = FxaClient.INSTANCE.fxa_get_sync_keys(this.validPointer(), e)
        if (e.isFailure()) throw FxaException.fromConsuming(e)!!
        return SyncKeys(p)
    }

    fun completeOAuthFlow(code: String, state: String): OAuthInfo {
        val e = Error.ByReference()
        val p = FxaClient.INSTANCE.fxa_complete_oauth_flow(this.validPointer(), code, state, e)
        if (e.isFailure()) throw FxaException.fromConsuming(e)!!
        return OAuthInfo(p)
    }

    fun getOAuthToken(scopes: Array<String>): OAuthInfo {
        val scope = scopes.joinToString(" ")
        val e = Error.ByReference()
        val p = FxaClient.INSTANCE.fxa_get_oauth_token(this.validPointer(), scope, e)
        if (e.isFailure()) throw FxaException.fromConsuming(e)!!
        return OAuthInfo(p)
    }

    companion object {
        fun from(config: Config, clientId: String, webChannelResponse: String): FxaResult<FirefoxAccount> {
            val e = Error.ByReference()
            val raw = FxaClient.INSTANCE.fxa_from_credentials(config.consumePointer(), clientId, webChannelResponse, e)
            if (e.isFailure()) {
                return FxaResult.fromException(FxaException.fromConsuming(e)!!)
            } else {
                return FxaResult.fromValue(FirefoxAccount(raw))
            }
        }

        fun fromJSONString(json: String): FxaResult<FirefoxAccount> {
            val e = Error.ByReference()
            val raw = FxaClient.INSTANCE.fxa_from_json(json, e)
            if (e.isFailure()) {
                return FxaResult.fromException(FxaException.fromConsuming(e)!!)
            } else {
                return FxaResult.fromValue(FirefoxAccount(raw))
            }
        }
    }
}

