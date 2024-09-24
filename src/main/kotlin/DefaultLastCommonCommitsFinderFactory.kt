class DefaultLastCommonCommitsFinderFactory : LastCommonCommitsFinderFactory {
    override fun create(owner: String, repo: String, token: String?): LastCommonCommitsFinder {
        return DefaultLastCommonCommitsFinder(owner, repo, token)
    }
}