import commitfetcher.DefaultGitHubCommitFetcher
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

open class DefaultGitHubCommitFetcherTest {
    companion object {
        lateinit var fetcherWithoutToken: DefaultGitHubCommitFetcher
        lateinit var fetcher: DefaultGitHubCommitFetcher

        @BeforeAll
        @JvmStatic
        fun setup() {
            fetcherWithoutToken = DefaultGitHubCommitFetcher("Tschonti", "common-commit-test", null)
            fetcher = DefaultGitHubCommitFetcher(
                "Tschonti",
                "common-commit-test",
                dotenv()["GITHUB_PAT"]
            )
        }
    }

    @Test
    fun `should throw error if the repository is private and no or invalid token was provided`() = runTest {
        assertFailsWith<IOException> { fetcherWithoutToken.fetchCommits("master") }
    }

    @Test
    fun `should throw error if non-existent branchName was provided`() = runTest {
        assertFailsWith<IOException> { fetcher.fetchCommits("non-existent-branch") }
    }

    @Test
    fun `should return all commits with no second parameter`() = runTest {
        val commits = fetcher.fetchCommits("branch-B")
        assertContains(commits.map { it.sha }, "0239179e4bfdb1237aa029bd0228c747da823e4d")
        assertContains(commits.map { it.sha }, "01f213baa6878e4245d8c320089ac8cdd912584b")
        assertEquals(2, commits.size)
    }

    @Test
    fun `should return the recent commits when a second parameter is provided`() = runTest {
        val cutOffDate = "2024-09-24T10:00:00Z"
        val commits = fetcher.fetchCommits("branch-B", cutOffDate)
        assertContains(commits.map { it.sha }, "0239179e4bfdb1237aa029bd0228c747da823e4d")
        assertEquals(1, commits.size)
        assertTrue(commits.first().date!! > cutOffDate)
    }
}