package commitfetcher

import Commit
import java.io.IOException

interface GitHubCommitFetcher {
    /**
     * Returns the commits from the GitHub repository based on a branch name or a commit SHA.
     * @param branchNameOrSha If it's a branch name, the commits of that branch will be returned.
     * If it's a commit SHA, the history of that commit (inclusive) will be returned.
     * @param since Timestamp in ISO 8601 format. If provided, only commits since that timestamp will be returned.
     * @return Collection of commits
     * @throws IOException if any errors occur
     */
    @Throws(IOException::class)
    suspend fun fetchCommits(branchNameOrSha: String, since: String? = null): Collection<Commit>
}