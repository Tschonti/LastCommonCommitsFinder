package commoncommitsfinder

import commitfetcher.DefaultGitHubCommitFetcher

class DefaultLastCommonCommitsFinderFactory : LastCommonCommitsFinderFactory {
    /**
     * Returns a LastCommonCommitsFinder instance that will always fetch commit data from GitHub's API.
     * @param owner owner of the GitHub repository
     * @param repo name of the GitHub repository
     * @param token PAT with read access to the contents of the repo. Only required if the repo is private.
     */
    override fun create(owner: String, repo: String, token: String?): LastCommonCommitsFinder {
        return DefaultLastCommonCommitsFinder(DefaultGitHubCommitFetcher(owner, repo, token))
    }
}