package app.util

import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.User
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.temporal.IsoFields
import java.time.temporal.IsoFields.QUARTER_YEARS
import java.time.temporal.TemporalAdjusters
import java.util.*

object CommitCountUtil {

    fun getCommitsForQuarters(user: User, repoCommits: Map<Repository, List<RepositoryCommit>>): SortedMap<String, Int> {
        val creation = asInstant(user.createdAt).withDayOfMonth(1)
        val now = Instant.now().atOffset(UTC).with(TemporalAdjusters.firstDayOfNextMonth())

        val quarterBuckets = (0..QUARTER_YEARS.between(creation, now))
                .associate { yearQuarterFromDate(creation.plus(it, QUARTER_YEARS)) to 0 }
                .toSortedMap()

        repoCommits.values.flatten().groupingBy { yearQuarterFromCommit(it) }.eachCountTo(quarterBuckets)

        return quarterBuckets
    }

    private fun asInstant(date: Date) = date.toInstant().atOffset(ZoneOffset.UTC)
    private fun yearQuarterFromCommit(it: RepositoryCommit) = yearQuarterFromDate(asInstant(it.commit.committer.date))
    private fun yearQuarterFromDate(date: OffsetDateTime) = "${date.year}-Q${date.get(IsoFields.QUARTER_OF_YEAR)}"

}
