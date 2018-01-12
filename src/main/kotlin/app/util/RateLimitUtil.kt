package app.util

import io.javalin.Javalin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * A very naive IP-based rate-limiting mechanism
 *
 * A HashMap holds IPs and counters, and the rules are
 * - incremented counter on every request
 * - decremented all counters every 5 seconds
 * - block request if counter > 25
 */
object RateLimitUtil {

    class TerribleRateLimitException : Exception()

    private val ipReqCount = ConcurrentHashMap<String, Int>()

    fun enableTerribleRateLimiting(app: Javalin) {

        app.before { ctx ->
            if (ipReqCount[ctx.ip()] ?: 0 > 25) {
                throw TerribleRateLimitException()
            }
            ipReqCount[ctx.ip()] = (ipReqCount[ctx.ip()] ?: 0) + 1
        }

        app.exception(TerribleRateLimitException::class.java) { e, ctx ->
            ctx.result("You can't spam this much. I'll give you a new request every five seconds.")
        }

        Timer().scheduleAtFixedRate(decrementAllCounters(), 0, 5000) // every 5s

    }

    private fun decrementAllCounters() = object : TimerTask() {
        override fun run() {
            ipReqCount.forEach { ip, count ->
                if (count > 0) {
                    ipReqCount[ip] = ipReqCount[ip]!! - 1
                } else {
                    ipReqCount.remove(ip)
                }
            }
        }
    }

}
