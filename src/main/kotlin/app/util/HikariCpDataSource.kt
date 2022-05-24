package app.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object HikariCpDataSource {
    private const val urlToDb = "jdbc:h2:mem:userinfo"

    private val config = HikariConfig()
    private var hikariDataSource: HikariDataSource?

    init {
        config.jdbcUrl = urlToDb
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        hikariDataSource = HikariDataSource(config)
    }

    val connection: Connection get() = hikariDataSource!!.connection
}
