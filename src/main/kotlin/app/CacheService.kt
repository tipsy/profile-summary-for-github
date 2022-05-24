@file:Suppress("SqlResolve")

package app

import app.util.HikariCpDataSource
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

object CacheService {
    private val log = LoggerFactory.getLogger(CacheService.javaClass)
    private val objectMapper = jacksonObjectMapper()

    private fun createTableIfAbsent() {
        val connection = HikariCpDataSource.connection
        val statement = connection.createStatement()

        statement.execute(
            "CREATE TABLE IF NOT EXISTS userinfo (" +
            "id VARCHAR2 PRIMARY KEY, " +
            "timestamp TIMESTAMP, " +
            "data JSON" +
            ")"
        )
    }

    fun selectJsonFromDb(username: String): String? {
        val connection = HikariCpDataSource.connection

        createTableIfAbsent()

        val preparedStatement = connection.prepareStatement(
            "SELECT " +
            "timestamp, " +
            "data " +
            "FROM userinfo " +
            "WHERE id = ?"
        )
        preparedStatement.setString(1, username.lowercase())

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

    fun getUserFromJson(json: String) = objectMapper.readValue<UserProfile>(json)

    fun saveInCache(userProfile: UserProfile) {
        val connection = HikariCpDataSource.connection

        createTableIfAbsent()

        val json = objectMapper.writeValueAsString(userProfile)

        val preparedStatement = connection.prepareStatement(
            "MERGE INTO userinfo (id, timestamp, data) KEY (id) " +
            "VALUES (?, CURRENT_TIMESTAMP(), ? FORMAT JSON)"
        )

        preparedStatement.setString(1, userProfile.user.login.lowercase())
        preparedStatement.setString(2, json)

        preparedStatement.execute()
    }
}