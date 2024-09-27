package commitfetcher

import Commit

class CachedGitHubCommitFetcher(private val baseFetcher: GitHubCommitFetcher) : GitHubCommitFetcher {
    private val cachedCommitData: MutableMap<String, Collection<Commit>> = mutableMapOf()

    /**
     * If the commit data for the same branch or commit has already been requested,
     * it will return that from memory without making a request to the GitHub API.
     * If the commit data is not fully available in memory,
     * it will call the GitHubCommitFetcher provided in the constructor to retrieve the commits,
     * then stores that data in memory.
     * @param branchNameOrSha If it's a branch name, the commits of that branch will be returned.
     * If it's a commit SHA, the history of that commit (inclusive) will be returned.
     * @param since Timestamp in ISO 8601 format. If provided, only commits since that timestamp will be returned.
     * @return Collection of commits
     */
    override suspend fun fetchCommits(branchNameOrSha: String, since: String?): Collection<Commit> {
        if (!cachedCommitData.containsKey(branchNameOrSha) || (since != null && cachedCommitData[branchNameOrSha]!!.last().date!! > since)) {
            val commits = baseFetcher.fetchCommits(branchNameOrSha, since)
            cachedCommitData[branchNameOrSha] = commits
            return commits
        }
        if (since != null && cachedCommitData[branchNameOrSha]!!.last().date!! < since) {
            return cachedCommitData[branchNameOrSha]!!.takeWhile { it.date!! > since }
        }
        return cachedCommitData[branchNameOrSha]!!
    }
}