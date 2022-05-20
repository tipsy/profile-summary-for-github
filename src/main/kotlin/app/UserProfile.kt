package app

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.UserPlan
import java.lang.reflect.Type
import java.util.Date

data class UserProfile(
    val user: User,
    val quarterCommitCount: Map<String, Int>,
    val langRepoCount: Map<String, Int>,
    val langStarCount: Map<String, Int>,
    val langCommitCount: Map<String, Int>,
    val repoCommitCount: Map<String, Int>,
    val repoStarCount: Map<String, Int>,
    val repoCommitCountDescriptions: Map<String, String?>,
    val repoStarCountDescriptions: Map<String, String?>
) {
    class Deserializer: JsonDeserializer<UserProfile> {
        override fun deserialize(jsonElement: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): UserProfile {
            if (jsonElement == null || context == null) {
                throw RuntimeException()
            }

            val jsonObject = jsonElement.asJsonObject

            val userObject = jsonObject.getAsJsonObject("user")
            val user = User()
            user.isHireable = userObject.get("hireable").asBoolean
            user.createdAt = Date(userObject.get("createdAt").asLong)
            user.collaborators = userObject.get("collaborators").asInt
            user.diskUsage = userObject.get("diskUsage").asInt
            user.followers = userObject.get("followers").asInt
            user.following = userObject.get("following").asInt
            user.id = userObject.get("id").asInt
            user.ownedPrivateRepos = userObject.get("ownedPrivateRepos").asInt
            user.privateGists = userObject.get("privateGists").asInt
            user.publicGists = userObject.get("publicGists").asInt
            user.publicRepos = userObject.get("publicRepos").asInt
            user.totalPrivateRepos = userObject.get("totalPrivateRepos").asInt
            user.avatarUrl = if (userObject.get("avatarUrl").isJsonNull) null else userObject.get("avatarUrl").asString
            user.blog = if (userObject.get("blog").isJsonNull) null else userObject.get("blog").asString
            user.company = if (userObject.get("company").isJsonNull) null else userObject.get("company").asString
            user.email = if (userObject.get("email").isJsonNull) null else userObject.get("email").asString
            user.gravatarId = if (userObject.get("gravatarId").isJsonNull) null else userObject.get("gravatarId").asString
            user.htmlUrl = if (userObject.get("htmlUrl").isJsonNull) null else userObject.get("htmlUrl").asString
            user.location = if (userObject.get("location").isJsonNull) null else userObject.get("location").asString
            user.login = if (userObject.get("login").isJsonNull) null else userObject.get("login").asString
            user.name = if (userObject.get("name").isJsonNull) null else userObject.get("name").asString
            user.type = if (userObject.get("type").isJsonNull) null else userObject.get("type").asString
            user.url = if (userObject.get("url").isJsonNull) null else userObject.get("url").asString
            user.plan = context.deserialize(userObject.get("plan"), UserPlan::class.java)

            val mapType1 = object: TypeToken<Map<String, Int>>() {}.type
            val mapType2 = object: TypeToken<Map<String, String?>>() {}.type

            val quarterCommitCount: Map<String, Int> = context.deserialize(jsonObject.get("quarterCommitCount"), mapType1)
            val langRepoCount: Map<String, Int> = context.deserialize(jsonObject.get("langRepoCount"), mapType1)
            val langStarCount: Map<String, Int> = context.deserialize(jsonObject.get("langStarCount"), mapType1)
            val langCommitCount: Map<String, Int> = context.deserialize(jsonObject.get("langCommitCount"), mapType1)
            val repoCommitCount: Map<String, Int> = context.deserialize(jsonObject.get("repoCommitCount"), mapType1)
            val repoStarCount: Map<String, Int> = context.deserialize(jsonObject.get("repoStarCount"), mapType1)
            val repoCommitCountDescriptions: Map<String, String?> = context.deserialize(jsonObject.get("repoCommitCountDescriptions"), mapType2)
            val repoStarCountDescriptions: Map<String, String?> = context.deserialize(jsonObject.get("repoStarCountDescriptions"), mapType2)

            return UserProfile(
                user,
                quarterCommitCount,
                langRepoCount,
                langStarCount,
                langCommitCount,
                repoCommitCount,
                repoStarCount,
                repoCommitCountDescriptions,
                repoStarCountDescriptions
            )
        }
    }
}
