fun main() {
//    val finder = DefaultLastCommonCommitsFinderFactory().create("kir-dev", "schbody", null)
    val finder = DefaultLastCommonCommitsFinderFactory().create(
        "Tschonti",
        "common-commit-test",
        "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" // fine-grained token, read-only access to a dummy repo
    )
//    val commits = finder.findLastCommonCommits("main", "feature/pass-export-optimize")
    val commits = finder.findLastCommonCommits("master", "branch-A")
    println(commits)
}