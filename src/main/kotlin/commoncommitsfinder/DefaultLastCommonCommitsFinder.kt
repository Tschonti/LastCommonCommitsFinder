package commoncommitsfinder

import Commit
import commitfetcher.GitHubCommitFetcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.collections.set

class DefaultLastCommonCommitsFinder(private val commitFetcher: GitHubCommitFetcher) : LastCommonCommitsFinder {
    override fun findLastCommonCommits(branchA: String, branchB: String): Collection<String> {
        return runBlocking {
            val branchACommits = async { commitFetcher.fetchCommits(branchA) }
            val branchBCommits = async { commitFetcher.fetchCommits(branchB) }

            val commonCommits = mutableSetOf<Commit>()
            val inspectedCommits =
                mutableMapOf<String, MutableList<String>>()  // parentCommitSha -> List<childCommitSha>

            val aCommits = branchACommits.await()
            aCommits.forEach { childCommit ->
                childCommit.parents?.forEach { parentCommit ->
                    if (inspectedCommits.containsKey(parentCommit.sha)) {
                        inspectedCommits[parentCommit.sha]?.add(childCommit.sha)
                    } else {
                        inspectedCommits[parentCommit.sha] = mutableListOf(childCommit.sha)
                    }
                }
            }

            val bCommits = branchBCommits.await()
            bCommits.forEach { childCommit ->
                childCommit.parents?.forEach { parentCommit ->
                    if (inspectedCommits.containsKey(parentCommit.sha) && inspectedCommits[parentCommit.sha]?.contains(
                            childCommit.sha
                        ) != true
                    ) {
                        commonCommits.add(parentCommit)
                    }
                }
            }
            return@runBlocking filterLastCommonCommits(commonCommits).map { it.sha }
        }
    }

    /**
     * Returns only those commits from commonCommits that are not reachable from other commits in the set.
     * Sorts the commits based on their creation date. Starting with the newest,
     * it fetches the commit history of each commit. If it finds any of the other commits in the history,
     * then those cannot be last common commits, so it will not return them at the end of the method.
     *
     * @param commonCommits Set of commits that are reachable from both branches
     * @return List of commits that are reachable from both branches, but not reachable from any other common commits
     */
    private suspend fun filterLastCommonCommits(commonCommits: Set<Commit>): List<Commit> {
        if (commonCommits.size < 2) {
            return commonCommits.toList()
        }
        val sortedCommits = commonCommits.sortedByDescending { it.date }
        val notLastCommitShas = mutableSetOf<String>()
        var oldestDate = commonCommits.last().date
        sortedCommits.forEach { commit ->
            if (commit.sha !in notLastCommitShas) {
                val commits = commitFetcher.fetchCommits(commit.sha, oldestDate).drop(1)
                for (c in commits) {
                    val childCommit = sortedCommits.firstOrNull { it.sha == c.sha }
                    if (childCommit != null) {
                        notLastCommitShas.add(childCommit.sha)
                        if (notLastCommitShas.size == commits.size - 1) {
                            break
                        }
                    }
                }
                oldestDate = sortedCommits.last { it.sha !in notLastCommitShas }.date
            }
        }
        return sortedCommits.filter { it.sha !in notLastCommitShas }
    }
}