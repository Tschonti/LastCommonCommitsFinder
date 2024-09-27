package commoncommitsfinder

import Commit
import commitfetcher.GitHubCommitFetcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.collections.set

class DefaultLastCommonCommitsFinder(private val commitFetcher: GitHubCommitFetcher) : LastCommonCommitsFinder {
    override fun findLastCommonCommits(branchA: String, branchB: String): Collection<String> {
        return runBlocking {
            val branchACommits = async { commitFetcher.getBranchCommits(branchA) }
            val branchBCommits = async { commitFetcher.getBranchCommits(branchB) }

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

    private suspend fun filterLastCommonCommits(commonCommits: Set<Commit>): List<Commit> {
        val sortedCommits = commonCommits.sortedByDescending { it.date }
        val notLastCommitShas = mutableSetOf<String>()
        var oldestDate = commonCommits.last().date
        sortedCommits.forEach { commit ->
            if (commit.sha !in notLastCommitShas) {
                val commits = commitFetcher.getBranchCommits(commit.sha, oldestDate).drop(1)
                commits.forEach { c ->
                    sortedCommits.firstOrNull { it.sha == c.sha }?.let { cc ->
                        notLastCommitShas.add(cc.sha)
                        oldestDate = sortedCommits.last { it.sha !in notLastCommitShas }.date
                    }
                }
            }
        }
        return sortedCommits.filter { it.sha !in notLastCommitShas }
    }
}