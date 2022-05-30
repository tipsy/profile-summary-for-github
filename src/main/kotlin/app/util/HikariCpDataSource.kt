package app.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object HikariCpDataSource {
    private const val urlToDb = "jdbc:h2:mem:userinfo"

    private val config = HikariConfig().apply {
        jdbcUrl = urlToDb
        addDataSourceProperty("cachePrepStmts", "true")
        addDataSourceProperty("prepStmtCacheSize", "250")
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    }

    private var hikariDataSource = HikariDataSource(config)

    val connection: Connection get() = hikariDataSource.connection
}
