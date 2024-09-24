import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.IOException

class DefaultLastCommonCommitsFinder(private val owner: String, private val repo: String, private val token: String?) :
    LastCommonCommitsFinder {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest {
            url {
                takeFrom("https://api.github.com/repos/$owner/$repo/commits")
            }
            token?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }

    override fun findLastCommonCommits(branchA: String, branchB: String): Collection<String> {
        return runBlocking {
            val branchACommits = async { getBranchCommits(branchA) }
            val branchBCommits = async { getBranchCommits(branchB) }

            val commonCommits = mutableSetOf<Commit>()
            val inspectedCommits = mutableMapOf<String, MutableList<String>>()  // parentCommitSha -> childCommitSha

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


            return@runBlocking commonCommits.map { it.sha }
        }
    }

    private suspend fun getBranchCommits(branchName: String): List<Commit> {
        var response = client.get("") {
            url {
                parameters.append("sha", branchName)
                parameters.append("per_page", "100")
            }
        }
        if (response.status == HttpStatusCode.OK) {
            val commitData: MutableList<Commit> = response.body()
            var nextPageHeader = this.extractNextPageHeader(response)
            while (nextPageHeader != null) {
                val urlRegex = "<(.*?)>".toRegex()
                val url = urlRegex.find(nextPageHeader)!!.groups[1]!!.value
                response = client.get(url)
                if (response.status == HttpStatusCode.OK) {
                    nextPageHeader = this.extractNextPageHeader(response)
                    commitData.addAll(response.body())
                } else {
                    throw IOException("Request to GitHub failed with status code ${response.status}")
                }
            }
            return commitData
        }
        if (response.status == HttpStatusCode.NotFound) {
            throw IOException("Request to GitHub failed with status code ${response.status}. If the repository is private, you have to set the token variable to a valid PAT!")
        }
        throw IOException("Request to GitHub failed with status code ${response.status}")

    }

    private fun extractNextPageHeader(res: HttpResponse): String? {
        return res.headers["link"]?.split(",")?.find { it.endsWith("rel=\"next\"") }
    }
}