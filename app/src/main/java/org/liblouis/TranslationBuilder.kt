package org.liblouis

class TranslationBuilder {
    var translationTable: TranslationTable? = null
        private set
    var inputCharacters: CharSequence? = ""
        private set
    var outputLength = 1
        private set
    var cursorOffset: Int? = null
        private set
    var includeHighlighting = false
        private set
    var allowLongerOutput = false
        private set

    fun setTranslationTable(table: TranslationTable?): TranslationBuilder {
        translationTable = table
        return this
    }

    fun setInputCharacters(characters: CharSequence?): TranslationBuilder {
        inputCharacters = characters
        return this
    }

    fun setOutputLength(length: Int): TranslationBuilder {
        outputLength = length
        return this
    }

    fun setCursorOffset(offset: Int): TranslationBuilder {
        cursorOffset = offset
        return this
    }

    fun setCursorOffset(): TranslationBuilder {
        cursorOffset = null
        return this
    }

    fun setIncludeHighlighting(yes: Boolean): TranslationBuilder {
        includeHighlighting = yes
        return this
    }

    fun setAllowLongerOutput(yes: Boolean): TranslationBuilder {
        allowLongerOutput = yes
        return this
    }

    private fun verifyValue(ok: Boolean, problem: String) {
        require(ok) { problem }
    }

    private fun verifyValues() {
        verifyValue(translationTable != null, "translation table not set")
        verifyValue(inputCharacters != null, "input characters not set")
        verifyValue(outputLength >= 0, "negative output length")
        verifyValue(cursorOffset == null || cursorOffset!! >= 0, "negative cursor offset")
    }

    fun newBrailleTranslation(): BrailleTranslation {
        verifyValues()
        return BrailleTranslation(this)
    }

    fun newTextTranslation(): TextTranslation {
        verifyValues()
        return TextTranslation(this)
    }
}