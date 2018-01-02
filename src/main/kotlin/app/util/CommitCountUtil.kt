package app.util

import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.User
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.MONTHS
import java.time.temporal.IsoFields
import java.util.*

object CommitCountUtil {

    fun getCommitsForQuarters(user: User, repoCommits: Map<Repository, List<RepositoryCommit>>): Map<String, Int> {
        val quarterBuckets = (0..MONTHS.between(asInstant(user.createdAt), asInstant(Date()).plusMonths(1))).associate { monthNr ->
            yearQuarterFromDate(asInstant(user.createdAt).plusMonths(monthNr)) to 0
        }.toSortedMap()
        repoCommits.flatMap { it.value }.forEach {
            quarterBuckets[yearQuarterFromCommit(it)] = (quarterBuckets[yearQuarterFromCommit(it)] ?: 0) + 1
        }
        return quarterBuckets
    }

    private fun asInstant(date: Date) = date.toInstant().atOffset(ZoneOffset.UTC)
    private fun yearQuarterFromCommit(it: RepositoryCommit) = yearQuarterFromDate(it.commit.committer.date.toInstant().atOffset(ZoneOffset.UTC))
    private fun yearQuarterFromDate(date: OffsetDateTime) = "${date.year}-Q${date.get(IsoFields.QUARTER_OF_YEAR)}"

}
