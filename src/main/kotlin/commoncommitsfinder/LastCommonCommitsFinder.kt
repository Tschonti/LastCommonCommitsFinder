package commoncommitsfinder

import java.io.IOException

interface LastCommonCommitsFinder {
    /**
     * Finds SHAs of last commits that are reachable from both
     * branchA and branchB
     *
     * @param branchA   branch name (e.g. "main")
     * @param branchB   branch name (e.g. "dev")
     * @return  a collection of SHAs of last common commits
     * @throws IOException  if any error occurs
     */
    @Throws(IOException::class)
    fun findLastCommonCommits(branchA: String, branchB: String): Collection<String>
}