import commoncommitsfinder.CachedLastCommonCommitsFinderFactory
import commoncommitsfinder.DefaultLastCommonCommitsFinderFactory
import commoncommitsfinder.LastCommonCommitsFinder
import io.github.cdimascio.dotenv.dotenv
import io.ktor.utils.io.errors.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

const val repoOwner = "Tschonti"
const val repository = "common-commit-test"

class LastCommonCommitsFinderTest {

    companion object {
        private val factory = DefaultLastCommonCommitsFinderFactory()
        private val cachedFactory = CachedLastCommonCommitsFinderFactory()

        @JvmStatic
        fun finderWithoutTokenProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(factory.create(repoOwner, repository, null)),
                Arguments.of(cachedFactory.create(repoOwner, repository, null))
            )
        }

        @JvmStatic
        fun finderProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(factory.create(repoOwner, repository, dotenv()["GITHUB_PAT"])),
                Arguments.of(cachedFactory.create(repoOwner, repository, dotenv()["GITHUB_PAT"]))
            )
        }
    }

    @ParameterizedTest
    @MethodSource("finderWithoutTokenProvider")
    fun `should throw error if the repository is private and no or invalid token was provided`(finder: LastCommonCommitsFinder) {
        assertFailsWith<IOException> { finder.findLastCommonCommits("master", "branch-A") }
    }

    @ParameterizedTest
    @MethodSource("finderProvider")
    fun `should throw error if non-existent branchName was provided`(finder: LastCommonCommitsFinder) {
        assertFailsWith<IOException> { finder.findLastCommonCommits("master", "non-existent-branch") }
    }

    @ParameterizedTest
    @MethodSource("finderProvider")
    fun `should find the last common commit for branches with simple history`(finder: LastCommonCommitsFinder) {
        val lastCommonCommits = finder.findLastCommonCommits("branch-C", "branch-D")
        assertContains(lastCommonCommits, "d3ac56bbd06378df41f2e3b761ec8de34ae07133")
        assertEquals(1, lastCommonCommits.size)
    }

    @ParameterizedTest
    @MethodSource("finderProvider")
    fun `should find all last common commits even with branches that have complicated history`(finder: LastCommonCommitsFinder) {
        val lastCommonCommits = finder.findLastCommonCommits("master", "branch-A")
        assertContains(lastCommonCommits, "a6dc1d725f7770dccdd7c74c9a7e4af72b518f86")
        assertContains(lastCommonCommits, "0239179e4bfdb1237aa029bd0228c747da823e4d")
        assertEquals(2, lastCommonCommits.size)
    }

    @ParameterizedTest
    @MethodSource("finderProvider")
    fun `the result should be the same if the two branches are swapped`(finder: LastCommonCommitsFinder) {
        val lastCommonCommitsA = finder.findLastCommonCommits("master", "branch-A")
        val lastCommonCommitsB = finder.findLastCommonCommits("branch-A", "master")
        assertEquals(lastCommonCommitsA.toSet(), lastCommonCommitsB.toSet())
    }
}