package com.absinthe.rulesbundle

private const val BRANCH_MASTER = "master"
private const val WORKING_BRANCH = BRANCH_MASTER

object Urls {
    const val GITHUB_ROOT_URL =
        "https://raw.githubusercontent.com/LibChecker/LibChecker-Rules/$WORKING_BRANCH/"
    const val GITLAB_ROOT_URL =
        "https://gitlab.com/zhaobozhen/LibChecker-Rules/-/raw/$WORKING_BRANCH/"
}
