package org.liblouis

import android.content.Context
import android.content.res.AssetManager
import android.preference.PreferenceManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class Louis(context: Context) {

    companion object {
        private const val LIBRARY_NAME = "louis"
        private val LOG_TAG = Louis::class.java.name

        val NATIVE_LOCK = Any()

        init {
            System.loadLibrary(LIBRARY_NAME)
        }
    }

    /**
     * This function should be called at the end of the application to free all memory allocated by liblouis.
     * Failure to do so will result in memory leaks.
     * Do NOT call this after each translation.
     * This will force liblouis to compile the translation tables every time they are used,
     * resulting in great inefficiency.
     */
    external fun releaseMemory()
    external fun getVersion(): String

    private external fun setDataPath(path: String)
    private external fun getDataPath(): String

    private external fun compileTranslationTable(table: String): Boolean
    private external fun setLogLevel(character: Char)
    external fun listTables(): Array<String>

    private val dataDirectory = context.getDir(LIBRARY_NAME, Context.MODE_PRIVATE)

    init {
        Log.i(LOG_TAG, "liblouis version: ${getVersion()}")
        setDataPath(dataDirectory.absolutePath)
        updatePackageData(context)
    }

    fun setLogLevel(level: LogLevel) {
        setLogLevel(level.character)
    }

    private fun removeFile(file: File) {
        if (file.isDirectory) {
            file.setWritable(true, true)
            for (name in file.list()) {
                removeFile(File(file, name))
            }
        }
        file.delete()
    }

    private fun extractAssets(
        assets: AssetManager,
        asset: String,
        location: File
    ) {
        try {
            val names = assets.list(asset)
            val isDirectory = names!!.isNotEmpty()
            val path = location.absolutePath
            if (isDirectory) {
                if (!location.exists()) {
                    if (!location.mkdir()) {
                        Log.w(LOG_TAG, "directory not created: $path")
                        return
                    }
                } else if (!location.isDirectory) {
                    Log.w(LOG_TAG, "not a directory: $path")
                    return
                }
                for (name in names) {
                    extractAssets(
                        assets,
                        File(asset, name).path,
                        File(location, name)
                    )
                }
            } else {
                val input = assets.open(asset)
                val output: OutputStream = FileOutputStream(location)
                val buffer = ByteArray(0X1000)
                var count: Int
                while (input.read(buffer).also { count = it } > 0) {
                    output.write(buffer, 0, count)
                }
                input.close()
                output.close()
            }
            location.setExecutable(isDirectory, false)
            location.setWritable(false, false)
            location.setReadable(true, false)
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "directory refresh error: " + exception.message)
        }
    }

    /**
     * Table files must originally be packaged as assets (they are not accessible from jni folder).
     * This is achieved via a symlink to `jni/liblouis/tables` in `assets`.
     * They then have to be moved to internal storage, to be accessible to liblouis, on installation.
     * This is the work here.
     * */
    private fun extractAssets(context: Context) {
        val assets = context.assets
        val assetsDirName = "liblouis"
        val oldName = "$assetsDirName.old"
        val newName = "$assetsDirName.new"
        val location = File(dataDirectory, assetsDirName)
        val oldLocation = File(dataDirectory, oldName)
        val newLocation = File(dataDirectory, newName)
        removeFile(oldLocation)
        removeFile(newLocation)
        extractAssets(assets, assetsDirName, newLocation)
        synchronized(NATIVE_LOCK) {
            location.renameTo(oldLocation)
            newLocation.renameTo(location)
            Log.d(LOG_TAG, "assets updated")
            releaseMemory()
        }
        removeFile(oldLocation)
    }

    private fun updatePackageData(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val file = File(context.packageCodePath)
        val prefKeySize = "package-size"
        val oldSize = prefs.getLong(prefKeySize, -1)
        val newSize = file.length()
        val prefKeyTime = "package-time"
        val oldTime = prefs.getLong(prefKeyTime, -1)
        val newTime = file.lastModified()
        if (newSize != oldSize || newTime != oldTime) {
            Log.d(LOG_TAG, "package size: $oldSize -> $newSize")
            Log.d(LOG_TAG, "package time: $oldTime -> $newTime")
            object : Thread() {
                override fun run() {
                    Log.d(LOG_TAG, "begin extracting assets")
                    extractAssets(context)
                    Log.d(LOG_TAG, "end extracting assets")
                    val editor = prefs.edit()
                    editor.putLong(prefKeySize, newSize)
                    editor.putLong(prefKeyTime, newTime)
                    editor.apply()
                }
            }.start()
        }
    }

    fun compileTranslationTable(table: File): Boolean {
        synchronized(NATIVE_LOCK) { return compileTranslationTable(table.absolutePath) }
    }

    fun compileTranslationTable(table: TranslationTable): Boolean {
        return compileTranslationTable(table.file)
    }

    fun getBrailleTranslation(
        table: TranslationTable, text: CharSequence
    ): BrailleTranslation {
        return TranslationBuilder()
            .setTranslationTable(table)
            .setInputCharacters(text)
            .setOutputLength(text.length * 2)
            .setAllowLongerOutput(true)
            .newBrailleTranslation()
    }

    fun getTextTranslation(
        table: TranslationTable, braille: CharSequence
    ): TextTranslation {
        return TranslationBuilder()
            .setTranslationTable(table)
            .setInputCharacters(braille)
            .setOutputLength(braille.length * 3)
            .setAllowLongerOutput(true)
            .newTextTranslation()
    }

    enum class LogLevel(val character: Char) {
        ALL('A'), DEBUG('D'), INFO('I'), WARN('W'), ERROR('E'), FATAL('F'), OFF('O')
    }
}