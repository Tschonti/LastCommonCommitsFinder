package commoncommitsfinder

import commitfetcher.CachedGitHubCommitFetcher
import commitfetcher.DefaultGitHubCommitFetcher

class CachedLastCommonCommitsFinderFactory : LastCommonCommitsFinderFactory {
    override fun create(owner: String, repo: String, token: String?): LastCommonCommitsFinder {
        return DefaultLastCommonCommitsFinder(CachedGitHubCommitFetcher(DefaultGitHubCommitFetcher(owner, repo, token)))
    }
}