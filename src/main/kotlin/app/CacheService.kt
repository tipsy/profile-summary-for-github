package app

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object CacheService {
    private const val urlToDb = "jdbc:h2:mem:userinfo"
    private val log = LoggerFactory.getLogger(CacheService.javaClass)

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

    fun lookUpInCache(username: String): UserProfile? {
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
                val timestamp = it.getTimestamp(1).toLocalDateTime()
                val diffInHours = ChronoUnit.HOURS.between(timestamp, LocalDateTime.now())
                if (diffInHours <= 6) {
                    val json: String = it.getString(2)

                    log.debug("cache hit: {}", json)

                    val simpleModule = SimpleModule()
                    simpleModule.addDeserializer(UserProfile::class.java, UserProfile.Deserializer())

                    val objectMapper = jacksonObjectMapper()
                    objectMapper.registerModule(simpleModule)

                    return objectMapper.readValue<UserProfile>(json)
                }
            }
        }

        log.debug("cache miss for username: {}", username)

        return null
    }

    fun saveInCache(userProfile: UserProfile) {
        val connection = DriverManager.getConnection(urlToDb)

        createTableIfAbsent()

        val json = jacksonObjectMapper().writeValueAsString(userProfile)

        val preparedStatement = connection.prepareStatement(
            "MERGE INTO userinfo (id, timestamp, data) KEY (id) " +
            "VALUES (?, CURRENT_TIMESTAMP(), ? FORMAT JSON)"
        )

        preparedStatement.setString(1, userProfile.user.login)
        preparedStatement.setString(2, json)

        preparedStatement.execute()
    }
}
