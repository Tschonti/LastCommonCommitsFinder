import io.github.cdimascio.dotenv.dotenv

fun main() {
//    val finder = DefaultLastCommonCommitsFinderFactory().create("kir-dev", "schbody", null)
    val finder = DefaultLastCommonCommitsFinderFactory().create(
        "Tschonti",
        "common-commit-test",
        dotenv()["GITHUB_PAT"]
    )
//    val commits = finder.findLastCommonCommits("main", "feature/pass-export-optimize")
    val commits = finder.findLastCommonCommits("master", "branch-A")
    println(commits)
}