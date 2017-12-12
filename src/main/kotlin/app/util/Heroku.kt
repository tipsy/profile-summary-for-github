package app.util

object Heroku {

    fun getPort() = ProcessBuilder().environment()["PORT"]?.let {
        Integer.parseInt(it)
    }

    fun getOauthToken() = ProcessBuilder().environment()["OAUTH_TOKEN"]

}
