package app

import java.io.*
import java.util.*

object Cache {

    private const val path = "cache/userinfo";

    fun putUserProfile(userProfile: UserProfile) {
        val userInfoMap = getAllUserInfo()
        userInfoMap[userProfile.user.login] = userProfile
        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).writeObject(userInfoMap)
        File(path).apply {
            if (!exists()) {
                parentFile.mkdirs()
                createNewFile()
            }
            writeBytes(byteArrayOutputStream.toByteArray())
        }
    }

    fun getUserInfo(username: String) = getAllUserInfo()[username]

    @Suppress("UNCHECKED_CAST")
    fun getAllUserInfo(): MutableMap<String, UserProfile> = if (File(path).exists()) {
        ObjectInputStream(
                ByteArrayInputStream(File("cache/userinfo").readBytes())
        ).readObject() as MutableMap<String, UserProfile>
    } else {
        mutableMapOf()
    }

    fun invalid(username: String) = Date().time - (getUserInfo(username)?.timeStamp ?: 0) > (60 * 60 * 24 * 1000) // 1 day in MD

}
