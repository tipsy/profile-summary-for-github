package app

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.UserPlan
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
    class Deserializer(valueClass: Class<*>?): StdDeserializer<UserProfile>(valueClass) {
        constructor(): this(null)

        override fun deserialize(jsonParser: JsonParser?, context: DeserializationContext?): UserProfile {
            if (jsonParser == null || context == null) {
                throw RuntimeException()
            }

            val userProfileNode: JsonNode = jsonParser.codec?.readTree(jsonParser) ?: throw RuntimeException()

            val userNode = userProfileNode.get("user")
            val user = User()
            user.isHireable = userNode.get("hireable").booleanValue()
            user.createdAt = Date(userNode.get("createdAt").longValue())
            user.collaborators = userNode.get("collaborators").intValue()
            user.diskUsage = userNode.get("diskUsage").intValue()
            user.followers = userNode.get("followers").intValue()
            user.following = userNode.get("following").intValue()
            user.id = userNode.get("id").intValue()
            user.ownedPrivateRepos = userNode.get("ownedPrivateRepos").intValue()
            user.privateGists = userNode.get("privateGists").intValue()
            user.publicGists = userNode.get("publicGists").intValue()
            user.publicRepos = userNode.get("publicRepos").intValue()
            user.totalPrivateRepos = userNode.get("totalPrivateRepos").intValue()
            user.avatarUrl = userNode.get("avatarUrl").textValue()
            user.blog = userNode.get("blog").textValue()
            user.company = userNode.get("company").textValue()
            user.email = userNode.get("email").textValue()
            user.gravatarId = userNode.get("gravatarId").textValue()
            user.htmlUrl = userNode.get("htmlUrl").textValue()
            user.location = userNode.get("location").textValue()
            user.login = userNode.get("login").textValue()
            user.name = userNode.get("name").textValue()
            user.type = userNode.get("type").textValue()
            user.url = userNode.get("url").textValue()

            val planNode = userNode.get("plan")
            user.plan = if (planNode.isNull) null else context.readValue(planNode.traverse(), UserPlan::class.java)

            val quarterCommitCount: MutableMap<String, Int> = parseInt(userProfileNode.get("quarterCommitCount"))
            val langRepoCount: MutableMap<String, Int> = parseInt(userProfileNode.get("langRepoCount"))
            val langStarCount: MutableMap<String, Int> = parseInt(userProfileNode.get("langStarCount"))
            val langCommitCount: MutableMap<String, Int> = parseInt(userProfileNode.get("langCommitCount"))
            val repoCommitCount: MutableMap<String, Int> = parseInt(userProfileNode.get("repoCommitCount"))
            val repoStarCount: MutableMap<String, Int> = parseInt(userProfileNode.get("repoStarCount"))
            val repoCommitCountDescriptions: MutableMap<String, String?> = parseString(userProfileNode.get("repoCommitCountDescriptions"))
            val repoStarCountDescriptions: MutableMap<String, String?> = parseString(userProfileNode.get("repoStarCountDescriptions"))

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

        private fun parseInt(jsonNode: JsonNode): MutableMap<String, Int> {
            val map: MutableMap<String, Int> = mutableMapOf()
            if (!jsonNode.isNull) {
                val fieldNames = jsonNode.fieldNames()
                while (fieldNames.hasNext()) {
                    val key = fieldNames.next()
                    val value = jsonNode[key].intValue()
                    map[key] = value
                }
            }

            return map
        }

        private fun parseString(jsonNode: JsonNode): MutableMap<String, String?> {
            val map: MutableMap<String, String?> = mutableMapOf()
            if (!jsonNode.isNull) {
                val fieldNames = jsonNode.fieldNames()
                while (fieldNames.hasNext()) {
                    val key = fieldNames.next()
                    val value = jsonNode[key].textValue()
                    map[key] = value
                }
            }

            return map
        }
    }
}
