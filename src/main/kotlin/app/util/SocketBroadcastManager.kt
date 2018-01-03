package app.util

import app.GhService
import io.javalin.embeddedserver.jetty.websocket.WsSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

object SocketBroadcastManager {

    // Keep track of the rate limit broadcast actions associated with each socket to easily cancel on close
    private val sessions = ConcurrentHashMap<WsSession, Future<*>>()

    // Directly construct this scheduled thread pool executor for setRemoveOnCancelPolicy(boolean) access
    // so that cancelled broadcast actions for closed sockets can be cleaned up more quickly
    // since java.util.concurrent.Executors returns too generic executors to do this call without reflection
    private val exec = ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors())

    fun registerSocket(ws: WsSession) {
        val future = exec.scheduleAtFixedRate(GhService.broadcastRemainingRequests(ws), 0, 1, TimeUnit.SECONDS)
        sessions.put(ws, future)
    }

    fun unregisterSocket(ws: WsSession) {
        sessions.remove(ws)?.cancel(true)
    }

    init {
        exec.removeOnCancelPolicy = true
    }

}
