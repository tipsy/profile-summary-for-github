package app.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object HikariCpDataSource {
    private const val urlToDb = "jdbc:h2:mem:userinfo;DB_CLOSE_DELAY=-1;QUERY_CACHE_SIZE=256"

    private val config = HikariConfig().apply {
        jdbcUrl = urlToDb
    }

    private var hikariDataSource = HikariDataSource(config)

    val connection: Connection get() = hikariDataSource.connection
}
