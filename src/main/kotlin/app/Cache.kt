package app

import java.io.*
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS

object Cache {

    private const val path = "cache/userinfo"
    private val userProfiles = readUserProfilesFromDisk()

    // Put userProfile in cache, then serialize cache and write it to disk
    fun putUserProfile(userProfile: UserProfile) {
        userProfiles[userProfile.user.login.toLowerCase()] = userProfile
        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).writeObject(userProfiles)
        File(path).apply {
            if (!exists()) {
                parentFile.mkdirs()
                createNewFile()
            }
            writeBytes(byteArrayOutputStream.toByteArray())
        }
    }

    fun getUserProfile(username: String) = userProfiles[username.toLowerCase()]
    fun contains(username: String) = getUserProfile(username) != null
    fun invalid(username: String): Boolean = getUserProfile(username)?.let {
        HOURS.between(Instant.ofEpochMilli(it.timeStamp), Instant.now()) > 6
    } ?: true

    // Read cache from disk, return empty map if no cache file exists
    @Suppress("UNCHECKED_CAST")
    private fun readUserProfilesFromDisk(): MutableMap<String, UserProfile> = if (File(path).exists()) {
        ObjectInputStream(
                ByteArrayInputStream(File("cache/userinfo").readBytes())
        ).readObject() as MutableMap<String, UserProfile>
    } else {
        mutableMapOf()
    }

}
