package app

import java.io.*
import java.util.*

object Cache {

    private const val path = "cache/userinfo";

    fun putUserProfile(userProfile: UserProfile) {
        val userProfileMap = getAllUserProfiles()
        userProfileMap[userProfile.user.login] = userProfile
        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).writeObject(userProfileMap)
        File(path).apply {
            if (!exists()) {
                parentFile.mkdirs()
                createNewFile()
            }
            writeBytes(byteArrayOutputStream.toByteArray())
        }
    }

    fun getUserProfile(username: String) = getAllUserProfiles()[username]

    @Suppress("UNCHECKED_CAST")
    fun getAllUserProfiles(): MutableMap<String, UserProfile> = if (File(path).exists()) {
        ObjectInputStream(
                ByteArrayInputStream(File("cache/userinfo").readBytes())
        ).readObject() as MutableMap<String, UserProfile>
    } else {
        mutableMapOf()
    }

    fun invalid(username: String) = Date().time - (getUserProfile(username)?.timeStamp ?: 0) > (60 * 60 * 24 * 1000) // 1 day in MD

}
