package app

import java.io.*
import java.util.*

object Cache {

    private const val path = "cache/userinfo";
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

    // Read cache from disk, return empty map if no cache file exists
    @Suppress("UNCHECKED_CAST")
    private fun readUserProfilesFromDisk(): MutableMap<String, UserProfile> = if (File(path).exists()) {
        ObjectInputStream(
                ByteArrayInputStream(File("cache/userinfo").readBytes())
        ).readObject() as MutableMap<String, UserProfile>
    } else {
        mutableMapOf()
    }

    fun contains(username: String) = userProfiles[username.toLowerCase()] != null
    fun invalid(username: String) = Date().time - (userProfiles[username.toLowerCase()]?.timeStamp ?: 0) > (60 * 60 * 24 * 1000) // 1 day in ms

}
