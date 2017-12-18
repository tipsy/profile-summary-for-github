package app

object Config {

    // Get port from Heroku, or return null (localhost)
    fun getPort() = ProcessBuilder().environment()["PORT"]?.let {
        Integer.parseInt(it)
    }

    // Get 'api-tokens' from Heroku/System, or return null if not set
    fun getApiTokens(): String? = ProcessBuilder().environment()["API_TOKENS"] ?: System.getProperty("api-tokens")

    // Get 'unrestricted' state from Heroku/System, or return null if not set
    fun getUnrestrictedState(): String? = ProcessBuilder().environment()["UNRESTRICTED"] ?: System.getProperty("unrestricted")

}
