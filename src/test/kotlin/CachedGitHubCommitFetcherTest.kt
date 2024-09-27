import commitfetcher.CachedGitHubCommitFetcher
import commitfetcher.DefaultGitHubCommitFetcher
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyNoMoreInteractions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CachedGitHubCommitFetcherTest : DefaultGitHubCommitFetcherTest() {
    private lateinit var cachedSpy: CachedGitHubCommitFetcher
    private lateinit var defaultSpy: DefaultGitHubCommitFetcher

    @BeforeEach
    fun setupSpies() {
        defaultSpy = spy(
            DefaultGitHubCommitFetcher(
                "Tschonti",
                "common-commit-test",
                dotenv()["GITHUB_PAT"]
            )
        )
        cachedSpy = spy(CachedGitHubCommitFetcher(defaultSpy))
    }


    @Test
    fun `should use the local cache when called multiple times with the same params`() = runTest {
        cachedSpy.fetchCommits("master")
        verify(cachedSpy).fetchCommits("master")
        verify(defaultSpy).fetchCommits("master")
        // the default implementation was called, so the commits were fetched from GitHub

        cachedSpy.fetchCommits("master")
        verify(cachedSpy, times(2)).fetchCommits("master")
        verifyNoMoreInteractions(defaultSpy)
        // the default implementation wasn't called, thus the cache was used and no new request was made to GitHub
    }

    @Test
    fun `should re-fetch the data when an older timestamp is provided`() = runTest {
        val tenAm = "2024-09-24T10:00:00Z"
        val nineAm = "2024-09-24T09:00:00Z"
        val commitsSince10Am = cachedSpy.fetchCommits("branch-B", tenAm)
        verify(cachedSpy).fetchCommits("branch-B", tenAm)
        verify(defaultSpy).fetchCommits("branch-B", tenAm)
        assertEquals(1, commitsSince10Am.size)

        val commitsSince9Am = cachedSpy.fetchCommits("branch-B", nineAm)
        verify(cachedSpy).fetchCommits("branch-B", nineAm)
        verify(defaultSpy).fetchCommits("branch-B", nineAm)
        assertEquals(2, commitsSince9Am.size)
    }

    @Test
    fun `should return only the most recent commits even if all the commits are cached`() = runTest {
        val tenAm = "2024-09-24T10:00:00Z"
        val allCommits = cachedSpy.fetchCommits("branch-B")
        assertEquals(2, allCommits.size)
        // all commits of branch-B are cached at this point

        // request commits only since 10 AM
        val commitsSince10Am = cachedSpy.fetchCommits("branch-B", tenAm)
        assertEquals(1, commitsSince10Am.size)
        assertTrue(commitsSince10Am.first().date!! > tenAm)
    }
}