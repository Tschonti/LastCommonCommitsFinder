import io.github.cdimascio.dotenv.dotenv
import io.ktor.utils.io.errors.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultLastCommonCommitsFinderTest {

    companion object {
        lateinit var finderWithoutToken: LastCommonCommitsFinder
        lateinit var finder: LastCommonCommitsFinder

        @BeforeAll
        @JvmStatic
        fun setup() {
            val factory = DefaultLastCommonCommitsFinderFactory()
            finderWithoutToken = factory.create("Tschonti", "common-commit-test", null)
            finder = factory.create(
                "Tschonti",
                "common-commit-test",
                dotenv()["GITHUB_PAT"]
            )
        }
    }

    @Test
    fun `should thrown error if the repository is private and no or invalid token was provided`() {
        assertFailsWith<IOException> { finderWithoutToken.findLastCommonCommits("master", "branch-A") }
    }

    @Test
    fun `should thrown error if non-existent branchName was provided`() {
        assertFailsWith<IOException> { finder.findLastCommonCommits("master", "non-existent-branch") }
    }

    @Test
    fun `should find all last common commits even with branches that have complicated history`() {
        val lastCommonCommits = finder.findLastCommonCommits("master", "branch-A")
        assertContains(lastCommonCommits, "a6dc1d725f7770dccdd7c74c9a7e4af72b518f86")
        assertContains(lastCommonCommits, "0239179e4bfdb1237aa029bd0228c747da823e4d")
        assertEquals(2, lastCommonCommits.size)
    }

    @Test
    fun `should find the last common commit for branches with simple history`() {
        val lastCommonCommits = finder.findLastCommonCommits("branch-C", "branch-D")
        assertContains(lastCommonCommits, "d3ac56bbd06378df41f2e3b761ec8de34ae07133")
        assertEquals(1, lastCommonCommits.size)
    }
}