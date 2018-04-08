package app

import java.io.*
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object Cache {

    private const val path = "cache/userinfo"
    private val userProfiles = readUserProfilesFromDisk()
    private val fileSaveExecutor = Executors.newSingleThreadExecutor()

    // Put userProfile in cache, then serialize cache and write it to disk
    fun putUserProfile(userProfile: UserProfile) {
        userProfiles[userProfile.user.login.toLowerCase()] = userProfile
        fileSaveExecutor.execute {
            ByteArrayOutputStream().let {
                ObjectOutputStream(it).writeObject(userProfiles)
                File(path).apply {
                    if (!exists()) {
                        parentFile.mkdirs()
                        createNewFile()
                    }
                    writeBytes(it.toByteArray())
                }
            }
        }
    }

    fun getUserProfile(username: String) = userProfiles[username.toLowerCase()]
    fun contains(username: String) = getUserProfile(username) != null
    fun invalid(username: String): Boolean = getUserProfile(username)?.let {
        HOURS.between(Instant.ofEpochMilli(it.timeStamp), Instant.now()) > 6
    } ?: true

    // Read cache from disk, return empty map if no cache file exists
    @Suppress("UNCHECKED_CAST")
    private fun readUserProfilesFromDisk(): ConcurrentHashMap<String, UserProfile> {
        try {
            File(path).apply {
                if (exists()) {
                    ObjectInputStream(ByteArrayInputStream(readBytes())).let {
                        val obj = it.readObject()

                        if (obj is ConcurrentHashMap<*, *>)
                            return obj as ConcurrentHashMap<String, UserProfile>
                    }
                }
            }
        } catch (e: Exception) {
        }

        return ConcurrentHashMap()
    }

}
