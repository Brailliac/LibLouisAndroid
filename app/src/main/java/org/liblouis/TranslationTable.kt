package org.liblouis

import java.io.File

// TODO: Add values for name and id, as read from meta data of file
data class TranslationTable(val filePath: String) {
    val file = File(filePath)
    val fileName = file.name
}