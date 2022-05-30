package com.absinthe.rulesbundle

sealed class LCRemoteRepo {
    object Github : LCRemoteRepo()
    object Gitlab : LCRemoteRepo()
}