package org.liblouis

class BrailleTranslation(builder: TranslationBuilder) : Translation(builder, false) {
    val suppliedText: CharSequence
        get() = suppliedInput!!

    val consumedText: CharSequence
        get() = consumedInput!!

    val textLength: Int
        get() = inputLength

    fun getTextOffset(brailleOffset: Int): Int {
        return getInputOffset(brailleOffset)
    }

    fun findFirstTextOffset(textOffset: Int): Int {
        return findFirstInputOffset(textOffset)
    }

    fun findLastTextOffset(textOffset: Int): Int {
        return findLastInputOffset(textOffset)
    }

    val textCursor: Int
        get() = inputCursor!!

    val brailleAsArray: CharArray
        get() = outputAsArray

    val brailleAsString: String
        get() = outputAsString!!

    val brailleWithSpans: CharSequence
        get() = outputWithSpans!!

    val brailleLength: Int
        get() = outputLength

    fun getBrailleOffset(textOffset: Int): Int {
        return getOutputOffset(textOffset)
    }

    fun findFirstBrailleOffset(brailleOffset: Int): Int {
        return findFirstOutputOffset(brailleOffset)
    }

    fun findLastBrailleOffset(brailleOffset: Int): Int {
        return findLastOutputOffset(brailleOffset)
    }

    val brailleCursor: Int
        get() = outputCursor!!
}