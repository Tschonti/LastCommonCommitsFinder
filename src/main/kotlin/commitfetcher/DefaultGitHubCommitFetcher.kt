package commitfetcher

import Commit
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.IOException

open class DefaultGitHubCommitFetcher(private val owner: String, private val repo: String, private val token: String?) :
    GitHubCommitFetcher {
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

    override suspend fun getBranchCommits(branchNameOrSha: String, since: String?): List<Commit> {
        var response = client.get("") {
            url {
                parameters.append("sha", branchNameOrSha)
                parameters.append("per_page", "100")
                since?.let { parameters.append("since", it) }
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