package commoncommitsfinder

import commitfetcher.CachedGitHubCommitFetcher
import commitfetcher.DefaultGitHubCommitFetcher

class CachedLastCommonCommitsFinderFactory : LastCommonCommitsFinderFactory {
    /**
     * Returns a LastCommonCommitsFinder instance that will try to resolve the commits of a branch from its own cache.
     * @param owner owner of the GitHub repository
     * @param repo name of the GitHub repository
     * @param token PAT with read access to the contents of the repo. Only required if the repo is private.
     */
    override fun create(owner: String, repo: String, token: String?): LastCommonCommitsFinder {
        return DefaultLastCommonCommitsFinder(CachedGitHubCommitFetcher(DefaultGitHubCommitFetcher(owner, repo, token)))
    }
}