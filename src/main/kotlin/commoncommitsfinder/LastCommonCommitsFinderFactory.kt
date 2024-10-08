package commoncommitsfinder

interface LastCommonCommitsFinderFactory {
    /**
     * Creates an instance of LastCommonCimmitsFinder.LastCommonCommitsFinder for a particular GitHub.com repository.
     * This method must not check connectivity.
     *
     * @param owner repository owner
     * @param repo  repository name
     * @param token personal access token or null for anonymous access
     * @return an instance of LastCommonCimmitsFinder.LastCommonCommitsFinder
     */
    fun create(owner: String, repo: String, token: String?): LastCommonCommitsFinder
}