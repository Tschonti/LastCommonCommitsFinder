package commitfetcher

import Commit

class CachedGitHubCommitFetcher(private val baseFetcher: GitHubCommitFetcher) : GitHubCommitFetcher {
    private val cachedCommitData: MutableMap<String, List<Commit>> = mutableMapOf()

    override suspend fun getBranchCommits(branchNameOrSha: String, since: String?): List<Commit> {
        if (!cachedCommitData.containsKey(branchNameOrSha) || (since != null && cachedCommitData[branchNameOrSha]!!.last().date!! > since)) {
            val commits = baseFetcher.getBranchCommits(branchNameOrSha, since)
            cachedCommitData[branchNameOrSha] = commits
            return commits
        }
        if (since != null && cachedCommitData[branchNameOrSha]!!.last().date!! < since) {
            return cachedCommitData[branchNameOrSha]!!.takeWhile { it.date!! > since }
        }
        return cachedCommitData[branchNameOrSha]!!
    }
}