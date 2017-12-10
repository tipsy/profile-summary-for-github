package app.util

fun getHerokuAssignedPort(): Int? {
    val processBuilder = ProcessBuilder()
    if (processBuilder.environment().get("PORT") != null) {
        return Integer.parseInt(processBuilder.environment().get("PORT"))
    }
    return null
}
