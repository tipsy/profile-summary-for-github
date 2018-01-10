package app.util

import io.javalin.Javalin
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
            ipReqCount.compute(ctx.ip(), { _, count ->
                when (count) {
                    null -> 1
                    in 0..25 -> count + 1
                    else -> throw TerribleRateLimitException()
                }
            })
        }

        app.exception(TerribleRateLimitException::class.java) { _, ctx ->
            ctx.result("You can't spam this much. I'll give you a new request every five seconds.")
        }

        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(decrementAllCounters, 0, 5, TimeUnit.SECONDS)

    }

    private val decrementAllCounters = Runnable {
        ipReqCount.forEachKey(1, { ip ->
            ipReqCount.computeIfPresent(ip, { _, count -> if (count > 1) count - 1 else null })
        })
    }

}
