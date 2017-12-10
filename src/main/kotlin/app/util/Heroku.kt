package app.util

object Heroku {

    fun getPort(): Int? {
        val processBuilder = ProcessBuilder()
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"))
        }
        return null
    }

    fun getOauthToken(): String? {
        val processBuilder = ProcessBuilder()
        return processBuilder.environment().get("OAUTH_TOKEN")
    }

}
