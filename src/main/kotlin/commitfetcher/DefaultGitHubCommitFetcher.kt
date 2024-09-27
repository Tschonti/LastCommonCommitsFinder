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

    /**
     * Fetches the commits using GitHub's REST API.
     * If there are more than 100 commits, it will send additional request to the API until all commits are retrieved.
     * @param branchNameOrSha If it's a branch name, the commits of that branch will be returned.
     * If it's a commit SHA, the history of that commit (inclusive) will be returned.
     * @param since Timestamp in ISO 8601 format. If provided, only commits since that timestamp will be returned.
     * @return Collection of commits
     * @throws IOException if any errors occur
     */
    override suspend fun fetchCommits(branchNameOrSha: String, since: String?): List<Commit> {
        var response = client.get("") {
            url {
                parameters.append("sha", branchNameOrSha)
                parameters.append("per_page", "100")
                since?.let { parameters.append("since", it) }
            }
        }
        if (response.status == HttpStatusCode.OK) {
            val commitData: MutableList<Commit> = response.body()
            var nextPageUrl = this.extractNextPageUrl(response)
            while (nextPageUrl != null) {
                response = client.get(nextPageUrl)
                if (response.status == HttpStatusCode.OK) {
                    nextPageUrl = this.extractNextPageUrl(response)
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


    /**
     * Extracts the URL for the next page of data from the HTTP response headers, if present.
     * Based on: https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api?apiVersion=2022-11-28
     * @param res HttpResponse from a paginated GitHub REST API endpoint
     * @return URL for the next page of data, or null in case the next page header was not present on the response
     */
    private fun extractNextPageUrl(res: HttpResponse): String? {
        val header = res.headers["link"]?.split(",")?.find { it.endsWith("rel=\"next\"") }
        if (header == null) {
            return null
        }
        val urlRegex = "<(.*?)>".toRegex()
        return urlRegex.find(header)!!.groups[1]!!.value
    }
}