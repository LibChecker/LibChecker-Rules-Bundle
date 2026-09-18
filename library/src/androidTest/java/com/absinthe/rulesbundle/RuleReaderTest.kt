package com.absinthe.rulesbundle

import android.database.sqlite.SQLiteDatabase
import android.os.Parcel
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class RuleReaderTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val root = File(context.cacheDir, "rules-reader-test").apply { deleteRecursively(); mkdirs() }

    @After fun cleanup() {
        LCRules.close()
        root.deleteRecursively()
    }

    @Test fun contextInitializationRepairsBaselineAndSeparatesUpdateActivation() {
        LCRules.init(context)
        val baseline = LCRules.getDatabaseFile()
        assertEquals(45L, LCRules.getMetadata().dataVersion)
        assertEquals("Flutter", query("libflutter.so")!!.label)
        assertEquals(setOf("rules.db", "metadata.json"), baseline.parentFile!!.list()!!.toSet())
        val invalid = File(root, "invalid.db").apply { writeText("invalid") }
        rejects { LCRules.activateDatabase(invalid) }
        assertEquals(baseline, LCRules.getDatabaseFile())
        assertEquals("Flutter", query("libflutter.so")!!.label)

        val update = fixture()
        insert(update, 1, "downloaded", false, 1)
        metadata(1)
        val updateMetadata = File(root, "metadata.json")
        updateMetadata.writeText(JSONObject(updateMetadata.readText()).put("dataVersion", 46).toString())
        LCRules.activateDatabase(update)
        assertEquals(46L, LCRules.getMetadata().dataVersion)
        assertEquals(update.canonicalFile, LCRules.getDatabaseFile())
        LCRules.init(context) // A fresh initialization always selects the AAR baseline first.
        assertEquals(baseline, LCRules.getDatabaseFile())
        assertEquals(45L, LCRules.getMetadata().dataVersion)

        LCRules.close()
        baseline.writeText("damaged cached database")
        File(baseline.parentFile, "metadata.json").writeText("damaged metadata")
        LCRules.init(context)
        assertEquals("Flutter", query("libflutter.so")!!.label)
        val owner = baseline.parentFile!!.parentFile!!
        assertEquals(setOf("bundled"), owner.list()!!.toSet())
        LCRules.close()
        assertTrue(baseline.parentFile!!.renameTo(File(owner, "backup")))
        File(owner, "staging").mkdir()
        File(owner, "staging/rules.db").writeText("interrupted copy")
        LCRules.init(context)
        assertEquals(baseline, LCRules.getDatabaseFile())
        assertEquals("Flutter", query("libflutter.so")!!.label)
        assertEquals(setOf("bundled"), owner.list()!!.toSet())
    }

    @Test fun rejectsV4DatabaseWithoutReplacingV5() {
        val current = fixture()
        insert(current, 1, "current", false, 1)
        metadata(1)
        LCRules.activateDatabase(current)
        val legacy = File(root, "v4.db")
        SQLiteDatabase.openOrCreateDatabase(legacy, null).use {
            it.execSQL("CREATE TABLE rules_table(_id INTEGER PRIMARY KEY,name TEXT,label TEXT," +
                "type INTEGER,iconIndex INTEGER,isRegexRule INTEGER,regexName TEXT)")
        }
        for (version in listOf(0, 4)) {
            update(legacy, "PRAGMA user_version=$version")
            rejects { RuleReader.open(legacy).close() }
            rejects { LCRules.activateDatabase(legacy) }
            assertEquals("1", query("current")!!.label)
            assertEquals(5, LCRules.getMetadata().schemaVersion)
            assertEquals(45L, LCRules.getMetadata().dataVersion)
        }
    }

    @Test fun v5PriorityExactPrecedenceCloudAndParcelable() {
        val db = fixture()
        insert(db, 1, ".*", true, 20)
        insert(db, 2, "foo|bar", true, 1)
        insert(db, 3, "foo", false, 100)
        metadata(3)
        RuleReader.open(db).use { reader ->
            assertEquals("3", reader.getRule("foo", NATIVE, true)!!.label)
            val rule = reader.getRule("bar", NATIVE, true)!!
            assertEquals("2", rule.label)
            assertEquals("2", reader.getRule("foo|bar", NATIVE, false)!!.label)
            assertEquals("1", reader.getRule("foobar", NATIVE, true)!!.label)
            assertNull(reader.getRule("bar", NATIVE, false))
            assertNull(reader.getRule("bar", SERVICE, true))
            assertNull(reader.getRule("bar\n", NATIVE, true))
            assertTrue(rule.descriptionUrl!!.endsWith("native-libs/foo|bar.json"))
            assertEquals(45L, reader.metadata.dataVersion)
            assertEquals(rule, rule.copy())
            assertEquals("changed", rule.copy(label = "changed").label)
            val parcel = Parcel.obtain()
            try {
                parcel.writeParcelable(rule, 0)
                parcel.setDataPosition(0)
                @Suppress("DEPRECATION")
                val restored = parcel.readParcelable<Rule>(Rule::class.java.classLoader)
                assertEquals(rule, restored)
                assertEquals(rule.hashCode(), restored.hashCode())
            } finally { parcel.recycle() }
        }
        // Seven-field data-class operations remain available.
        val old = Rule("name", 0, "label", 0, null, null, false)
        val (name, type, label) = old.copy()
        assertEquals("name", name)
        assertEquals(0, type)
        assertEquals("label", label)
        assertTrue(Rule::class.java.declaredMethods.any { it.name == "copy\$default" && it.parameterCount == 10 })
    }

    @Test fun rejectsUnpublishedExpandedSchemaWithoutReplacingReader() {
        val current = fixture()
        insert(current, 1, "current", false, 1)
        metadata(1)
        LCRules.activateDatabase(current)
        val expanded = File(root, "expanded.db")
        current.copyTo(expanded)
        for (column in listOf("uuid TEXT", "iconId TEXT", "detailPath TEXT", "isSimpleColorIcon INTEGER")) {
            update(expanded, "ALTER TABLE rules_table ADD COLUMN $column")
        }
        rejects { LCRules.activateDatabase(expanded) }
        assertEquals(current.canonicalFile, LCRules.getDatabaseFile())
        assertEquals("1", query("current")!!.label)
    }

    @Test fun rejectsBadCandidateWithoutReplacingCurrentReader() {
        val good = fixture()
        insert(good, 1, "foo", false, 1)
        metadata(1)
        LCRules.activateDatabase(good)
        assertEquals("1", query("foo")!!.label)
        val bad = File(root, "bad.db").apply { writeText("not SQLite") }
        rejects { LCRules.activateDatabase(bad) }
        assertEquals("not SQLite", bad.readText())
        assertEquals(45L, LCRules.getMetadata().dataVersion)
        assertEquals("1", query("foo")!!.label)
    }

    @Test fun eagerlyRejectsMalformedSchemaAndRegex() {
        val db = fixture()
        insert(db, 1, "[", true, 1)
        metadata(1)
        rejects { RuleReader.open(db).close() }
        update(db, "UPDATE rules_table SET name='ok', priority=-1")
        rejects { RuleReader.open(db).close() }
        update(db, "UPDATE rules_table SET priority=1, type=99")
        rejects { RuleReader.open(db).close() }
        update(db, "UPDATE rules_table SET type=0, isRegexRule=7")
        rejects { RuleReader.open(db).close() }
        update(db, "UPDATE rules_table SET isRegexRule=0")
        metadata(2)
        rejects { RuleReader.open(db).close() }
        metadata(1)
        update(db, "PRAGMA user_version=6")
        rejects { RuleReader.open(db).close() }
        update(db, "PRAGMA user_version=0")
        rejects { RuleReader.open(db).close() }
    }

    @Test fun portableAsciiDigitsRespectEscapingAndCharacterClasses() {
        val db = fixture()
        insert(db, 1, "lib\\d+", true, 1)
        insert(db, 2, "class[\\d]+", true, 2)
        insert(db, 3, "literal\\\\d", true, 3)
        insert(db, 4, "[^x]+", true, 4)
        metadata(4)
        RuleReader.open(db).use {
            assertEquals("1", it.getRule("lib12", NATIVE, true)!!.label)
            assertEquals("4", it.getRule("lib١", NATIVE, true)!!.label)
            assertEquals("2", it.getRule("class12", NATIVE, true)!!.label)
            assertEquals("4", it.getRule("class١", NATIVE, true)!!.label)
            assertEquals("3", it.getRule("literal\\d", NATIVE, true)!!.label)
            for (separator in "\n\r\u0085\u2028\u2029") {
                assertNull(it.getRule("a${separator}b", NATIVE, true))
            }
        }
    }

    @Test fun priorityTiesUseIdAndClosedReaderRejectsQueries() {
        val db = fixture()
        insert(db, 20, ".*", true, 1)
        insert(db, 10, "foo|bar", true, 1)
        metadata(2)
        val reader = RuleReader.open(db)
        assertEquals("10", reader.getRule("bar", NATIVE, true)!!.label)
        reader.close()
        reader.close()
        rejects { reader.getRule("bar", NATIVE, true) }
    }

    @Test fun readsRealDatabaseOnlyArtifact() {
        val assets = instrumentation.context.assets
        assumeTrue("Pass -PrulesTestBundleDir=<unpacked android-v5.zip>", assets.list("")!!.contains("rules.db"))
        fun copyAssets(path: String) {
            val children = assets.list(path)!!
            if (children.isEmpty()) {
                val output = File(root, path).apply { parentFile!!.mkdirs() }
                assets.open(path).use { input -> output.outputStream().use { input.copyTo(it) } }
            } else children.forEach { copyAssets(if (path.isEmpty()) it else "$path/$it") }
        }
        listOf("rules.db", "metadata.json").forEach(::copyAssets)
        assertEquals(setOf("rules.db", "metadata.json"), root.list()!!.toSet())
        RuleReader.open(File(root, "rules.db")).use { reader ->
            assertTrue(reader.metadata.ruleCount >= 2832)
            val flutter = reader.getRule("libflutter.so", NATIVE, false)!!
            assertEquals("Flutter", flutter.label)
            assertTrue(reader.metadata.dataVersion >= 45)
            assertEquals(IconResMap.getIconRes(20), flutter.iconRes)
            assertEquals("${LCRemoteRepo.GitHub.rootUrl}native-libs/libflutter.so.json", flutter.descriptionUrl)
            // Every v5 row still resolves literally, including regex pattern strings.
            SQLiteDatabase.openDatabase(File(root, "rules.db").path, null, SQLiteDatabase.OPEN_READONLY).use { source ->
                source.rawQuery("SELECT name,type,label FROM rules_table", null).use { rows ->
                    while (rows.moveToNext()) {
                        assertEquals(rows.getString(2), reader.getRule(rows.getString(0), rows.getInt(1), false)?.label)
                    }
                }
            }
        }
    }

    @Test fun databaseOnlyUsesCloudDetailsAndDrawableFallback() {
        val db = fixture()
        insert(db, 1, "libx[0-9]+", true, 1)
        metadata(1)
        update(db, "UPDATE rules_table SET iconIndex=999999, regexName='libx_pattern'")
        RuleReader.open(db).use { reader ->
            val rule = reader.getRule("libx12", NATIVE, true, LCRemoteRepo.GitLab)!!
            assertEquals("${LCRemoteRepo.GitLab.rootUrl}native-libs/libx_pattern.json", rule.descriptionUrl)
            assertEquals(IconResMap.getIconRes(-1), rule.iconRes)
            assertTrue(rule.isSimpleColorIcon)
            assertFalse(File(root, "icons").exists())
            assertFalse(File(root, "details").exists())
        }
        update(db, "UPDATE rules_table SET iconIndex=20")
        LCRules.activateDatabase(db)
        LCRules.setRemoteRepo(LCRemoteRepo.GitHub)
        val known = query("libx12")!!
        assertEquals(IconResMap.getIconRes(20), known.iconRes)
        assertFalse(known.isSimpleColorIcon) // Bundle resource metadata.
        assertEquals("${LCRemoteRepo.GitHub.rootUrl}native-libs/libx_pattern.json", known.descriptionUrl)
    }

    private fun fixture(name: String = "rules.db"): File = File(root, name).also { file ->
        SQLiteDatabase.openOrCreateDatabase(file, null).use {
            it.execSQL("CREATE TABLE rules_table(_id INTEGER PRIMARY KEY,name TEXT NOT NULL,label TEXT NOT NULL," +
                "type INTEGER NOT NULL,iconIndex INTEGER NOT NULL,isRegexRule INTEGER NOT NULL,regexName TEXT," +
                "priority INTEGER NOT NULL)")
            it.version = 5
        }
    }

    private fun insert(file: File, id: Int, name: String, regex: Boolean, priority: Int,
                       type: Int = NATIVE) {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use {
            it.execSQL("INSERT INTO rules_table VALUES(?,?,?,?,?,?,?,?)",
                arrayOf<Any?>(id, name, id.toString(), type, -1, if (regex) 1 else 0, null, priority))
        }
    }

    private fun metadata(count: Int) {
        File(root, "metadata.json").writeText(JSONObject().put("schemaVersion", 5).put("dataVersion", 45)
            .put("ruleCount", count).put("sourceRevision", "a".repeat(40)).put("compilerRevision", "b".repeat(64))
            .put("contentSha256", "c".repeat(64)).put("minimumReader", JSONObject().put("android", 5)).toString())
    }

    private fun update(file: File, sql: String) {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { it.execSQL(sql) }
    }

    private fun rejects(block: () -> Unit) {
        try { block() } catch (_: Exception) { return }
        fail("Expected invalid input to fail")
    }

    private fun query(name: String): Rule? {
        var outcome: Result<Rule?>? = null
        suspend { LCRules.getRule(name, NATIVE, true) }.startCoroutine(object : Continuation<Rule?> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<Rule?>) { outcome = result }
        })
        return outcome!!.getOrThrow()
    }
}
