package app

import org.eclipse.egit.github.core.User

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
)
