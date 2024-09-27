package commoncommitsfinder

import commitfetcher.DefaultGitHubCommitFetcher

class DefaultLastCommonCommitsFinderFactory : LastCommonCommitsFinderFactory {
    override fun create(owner: String, repo: String, token: String?): LastCommonCommitsFinder {
        return DefaultLastCommonCommitsFinder(DefaultGitHubCommitFetcher(owner, repo, token))
    }
}