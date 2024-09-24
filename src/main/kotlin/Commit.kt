import kotlinx.serialization.Serializable

@Serializable
data class Commit(val sha: String, val parents: List<Commit>? = null)
