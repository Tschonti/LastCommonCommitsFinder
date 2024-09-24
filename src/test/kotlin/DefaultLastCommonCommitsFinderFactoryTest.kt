import kotlin.test.Test
import kotlin.test.fail

class DefaultLastCommonCommitsFinderFactoryTest {

    @Test
    fun `factory method should succeed even with private repository and no token`() {
        val factory = DefaultLastCommonCommitsFinderFactory()
        try {
            factory.create("Tschonti", "common-commit-test", null)
        } catch (e: Exception) {
            fail("CommonCommitsFinder factory method failed to create instance for a private repository without a token")
        }
    }
}