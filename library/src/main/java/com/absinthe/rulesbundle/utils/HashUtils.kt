package com.absinthe.rulesbundle.utils

import java.security.MessageDigest

object HashUtils {
    fun md5(byteArray: ByteArray): String = digest(byteArray, "MD5")

    fun sha1(byteArray: ByteArray): String = digest(byteArray, "SHA-1")

    fun sha256(byteArray: ByteArray): String = digest(byteArray, "SHA-256")

    fun sha512(byteArray: ByteArray): String = digest(byteArray, "SHA-512")

    private fun digest(byteArray: ByteArray, algorithm: String): String {
        val messageDigest = MessageDigest.getInstance(algorithm).apply {
            reset()
        }
        messageDigest.update(byteArray)
        return messageDigest.digest().toHexString()
    }

    private fun ByteArray.toHexString() : String {
        return this.joinToString("") { it.toString(16) }
    }
}