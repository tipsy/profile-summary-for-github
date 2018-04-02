package app

object Config {

    // Get port from Heroku, or return null (localhost)
    fun getPort() = getHerokuProperty("port")?.let {
        Integer.parseInt(it)
    }

    // Get 'api-tokens' from Heroku/System, or return null if not set
    fun getApiTokens(): String? = getProperty("api-tokens")

    // Get 'unrestricted' state from Heroku/System, or return null if not set
    fun getUnrestrictedState(): String? = getProperty("unrestricted")

    // Get 'gtm-id' from Heroku/System, or return null if not set
    fun getGtmId(): String? = getProperty("gtm-id")

    // Get 'star-bypass' from Heroku/System, or return null if not stored
    fun freeRequestCutoff() = getProperty("free-requests-cutoff")?.let { Integer.parseInt(it) }

    private fun getProperty(name: String): String? = getHerokuProperty(name) ?: System.getProperty(name)

    private fun getHerokuProperty(envStr: String) = ProcessBuilder().environment()[envStr.toUpperCase().replace("-", "_")]

}
