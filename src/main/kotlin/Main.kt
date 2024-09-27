import commoncommitsfinder.DefaultLastCommonCommitsFinderFactory
import io.github.cdimascio.dotenv.dotenv

fun main() {
    val finder = DefaultLastCommonCommitsFinderFactory().create(
        "Tschonti",
        "common-commit-test",
        dotenv()["GITHUB_PAT"]
    )
    val commits = finder.findLastCommonCommits("master", "branch-A")
    println(commits)
}