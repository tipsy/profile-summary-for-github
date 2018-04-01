package app

object Config {

    // Get port from Heroku, or return null (localhost)
    fun getPort() = ProcessBuilder().environment()["PORT"]?.let {
        Integer.parseInt(it)
    }

    // Get 'api-tokens' from Heroku/System, or return null if not set
    fun getApiTokens(): String? = getField("API_TOKENS", "api-tokens")

    // Get 'unrestricted' state from Heroku/System, or return null if not set
    fun getUnrestrictedState(): String? = getField("UNRESTRICTED", "unrestricted")

    // Get 'gtm-id' from Heroku/System, or return null if not set
    fun getGtmId(): String? = getField("GTM_ID", "gtm-id")

    // Get 'star-bypass' from Heroku/System, or return null if not stored
    fun getStarRequestBypassLevel() = getField("STAR_BYPASS", "star-bypass")?.let { Integer.parseInt(it) }

    private fun getField(envStr: String, sysStr: String): String? = ProcessBuilder().environment()[envStr]
            ?: System.getProperty(sysStr)

}
