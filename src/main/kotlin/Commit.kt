import kotlinx.serialization.Serializable

@Serializable
data class Commit(
    val sha: String, val commit: InnerCommit? = null, val parents: List<Commit>? = null
) {
    val date: String?
        get() = commit?.author?.date
}

@Serializable
data class InnerCommit(
    val author: Author
)

@Serializable
data class Author(
    val date: String
)