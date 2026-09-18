package com.absinthe.rulesbundle

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

/** Owns the bundled v5 baseline; the app validates and installs downloaded updates. */
object LCRules {
    private var remoteRepo: LCRemoteRepo = LCRemoteRepo.GitHub
    private var reader: RuleReader? = null
    private var databaseFile: File? = null

    /** Initialize the sole bundled baseline without requiring a caller-supplied DB path. */
    @Synchronized
    fun init(context: Context) {
        val app = context.applicationContext
        val root = File(app.noBackupFilesDir, "lcrules-v5").apply { check(mkdirs() || isDirectory) }
        val bundled = File(root, "bundled")
        val backup = File(root, "backup")
        val staging = File(root, "staging")
        val names = listOf("rules.db", "metadata.json")
        // Recover a directory rename interrupted before the new baseline was published.
        if (!bundled.exists() && backup.exists()) check(backup.renameTo(bundled))
        val expected = names.associateWith { name -> app.assets.open("lcrules/v5/$name").use(::sha256) }
        val intact = bundled.list()?.toSet() == names.toSet() && names.all { name ->
            val file = File(bundled, name)
            file.isFile && runCatching { file.inputStream().use(::sha256).contentEquals(expected.getValue(name)) }.getOrDefault(false)
        }
        if (intact) {
            activateDatabase(File(bundled, "rules.db"))
            check(backup.deleteRecursively())
            check(staging.deleteRecursively())
            return
        }
        check(staging.deleteRecursively())
        check(staging.mkdir())
        try {
            for (name in names) {
                app.assets.open("lcrules/v5/$name").use { input ->
                    FileOutputStream(File(staging, name)).use { output ->
                        input.copyTo(output)
                        output.fd.sync()
                    }
                }
                check(File(staging, name).inputStream().use(::sha256).contentEquals(expected.getValue(name)))
            }
            RuleReader.open(File(staging, "rules.db")).use { }
            check(backup.deleteRecursively())
            if (bundled.exists()) check(bundled.renameTo(backup))
            if (!staging.renameTo(bundled)) {
                check(!backup.exists() || backup.renameTo(bundled))
                error("Cannot publish bundled rules")
            }
            try {
                activateDatabase(File(bundled, "rules.db"))
            } catch (failure: Throwable) {
                check(bundled.deleteRecursively())
                check(!backup.exists() || backup.renameTo(bundled))
                throw failure
            }
            check(backup.deleteRecursively())
        } finally {
            staging.deleteRecursively()
        }
    }

    private fun sha256(input: InputStream): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
        return digest.digest()
    }

    /** Opens an app-selected DB; a failed open keeps the current reader and metadata intact. */
    @Synchronized
    fun activateDatabase(databaseFile: File) {
        val selected = databaseFile.canonicalFile
        val candidate = RuleReader.open(selected)
        val previous = reader
        reader = candidate
        this.databaseFile = selected
        previous?.close()
    }

    @Synchronized
    fun close() {
        reader?.close()
        reader = null
        databaseFile = null
    }

    @Synchronized
    fun getDatabaseFile(): File = checkNotNull(databaseFile) { "LCRules is not initialized" }

    @Synchronized
    fun getMetadata(): RulesMetadata = checkNotNull(reader) { "LCRules is not initialized" }.metadata

    suspend fun getRule(libName: String, @LibType type: Int, useRegex: Boolean): Rule? =
        synchronized(this) { reader?.getRule(libName, type, useRegex, remoteRepo) }

    @Synchronized
    fun setRemoteRepo(repo: LCRemoteRepo) {
        remoteRepo = repo
    }
}
