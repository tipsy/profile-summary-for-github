@file:Suppress("SqlResolve")

package app

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.sql.DriverManager
import java.time.Instant
import java.time.temporal.ChronoUnit

object CacheService {
    private const val urlToDb = "jdbc:h2:mem:userinfo"
    private val log = LoggerFactory.getLogger(CacheService.javaClass)
    private val objectMapper = jacksonObjectMapper()

    private fun createTableIfAbsent() {
        val connection = DriverManager.getConnection(urlToDb)
        val statement = connection.createStatement()

        statement.execute(
            "CREATE TABLE IF NOT EXISTS userinfo (" +
            "id VARCHAR2 PRIMARY KEY," +
            "timestamp TIMESTAMP WITH TIME ZONE, " +
            "data JSON" +
            ")"
        )
    }

    fun selectJsonFromDb(username: String): String? {
        val connection = DriverManager.getConnection(urlToDb)

        createTableIfAbsent()

        val preparedStatement = connection.prepareStatement(
            "SELECT " +
            "timestamp, " +
            "data " +
            "FROM userinfo " +
            "WHERE id = ?"
        )
        preparedStatement.setString(1, username)

        val result = preparedStatement.executeQuery()
        result.use {
            // guaranteed to be at most one.
            if (it.next()) {
                val timestamp = it.getTimestamp(1).toInstant()
                val diffInHours = ChronoUnit.HOURS.between(timestamp, Instant.now())
                if (diffInHours <= 6) {
                    val json: String? = it.getString(2)
                    if (json != null) {
                        log.debug("cache hit: {}", json)
                    }

                    return json
                }
            }
        }

        log.debug("cache miss for username: {}", username)

        return null
    }

    fun getUserFromCache(username: String) = selectJsonFromDb(username)?.let { json -> objectMapper.readValue<UserProfile>(json) }

    fun saveInCache(userProfile: UserProfile) {
        val connection = DriverManager.getConnection(urlToDb)

        createTableIfAbsent()

        val json = objectMapper.writeValueAsString(userProfile)

        val preparedStatement = connection.prepareStatement(
            "MERGE INTO userinfo (id, timestamp, data) KEY (id) " +
            "VALUES (?, CURRENT_TIMESTAMP(), ? FORMAT JSON)"
        )

        preparedStatement.setString(1, userProfile.user.login)
        preparedStatement.setString(2, json)

        preparedStatement.execute()
    }
}
