package commitfetcher

import Commit
import java.io.IOException

interface GitHubCommitFetcher {
    @Throws(IOException::class)
    suspend fun getBranchCommits(branchNameOrSha: String, since: String? = null): List<Commit>
}