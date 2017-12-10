package app

import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.User
import java.io.*
import java.util.*

data class UserInfo(
        val username: String,
        val user: User,
        val repos: List<Repository>,
        val repoCommits: Map<Repository, List<RepositoryCommit>>) : Serializable {
    val timeStamp = Date().time
}

object Cache {

    private const val path = "cache/userinfo";

    fun putUserInfo(userInfo: UserInfo) {
        val userInfoMap = getAllUserInfo()
        userInfoMap[userInfo.username] = userInfo
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
    fun getAllUserInfo(): MutableMap<String, UserInfo> = if (File(path).exists()) {
        ObjectInputStream(
                ByteArrayInputStream(File("cache/userinfo").readBytes())
        ).readObject() as MutableMap<String, UserInfo>
    } else {
        mutableMapOf()
    }

    fun invalid(username: String) = Date().time - (getUserInfo(username)?.timeStamp ?: 0) > (60 * 60 * 24 * 1000) // 1 day in MD

}
