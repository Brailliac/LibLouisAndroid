package org.liblouis


class TranslationTableList(louis: Louis) {
    val tables = louis.listTables().map {
        TranslationTable(it)
    }
}