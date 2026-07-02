package com.absinthe.rulesbundle

enum class LCRemoteRepo(internal val rootUrl: String) {
    GitHub(Urls.GITHUB_ROOT_URL),
    GitLab(Urls.GITLAB_ROOT_URL);

    companion object {
        @Deprecated("Use GitHub.", ReplaceWith("LCRemoteRepo.GitHub"))
        val Github: LCRemoteRepo = GitHub

        @Deprecated("Use GitLab.", ReplaceWith("LCRemoteRepo.GitLab"))
        val Gitlab: LCRemoteRepo = GitLab
    }
}
