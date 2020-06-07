package org.liblouis

class TextTranslation(builder: TranslationBuilder) : Translation(builder, true) {
    val suppliedBraille: CharSequence
        get() = suppliedInput!!

    val consumedBraille: CharSequence
        get() = consumedInput!!

    val brailleLength: Int
        get() = inputLength

    fun getBrailleOffset(textOffset: Int): Int {
        return getInputOffset(textOffset)
    }

    fun findFirstBrailleOffset(brailleOffset: Int): Int {
        return findFirstInputOffset(brailleOffset)
    }

    fun findLastBrailleOffset(brailleOffset: Int): Int {
        return findLastInputOffset(brailleOffset)
    }

    val brailleCursor: Int
        get() = inputCursor!!

    val textAsArray: CharArray
        get() = outputAsArray

    val textAsString: String
        get() = outputAsString!!

    val textWithSpans: CharSequence
        get() = outputWithSpans!!

    val textLength: Int
        get() = outputLength

    fun getTextOffset(brailleOffset: Int): Int {
        return getOutputOffset(brailleOffset)
    }

    fun findFirstTextOffset(textOffset: Int): Int {
        return findFirstOutputOffset(textOffset)
    }

    fun findLastTextOffset(textOffset: Int): Int {
        return findLastOutputOffset(textOffset)
    }

    val textCursor: Int
        get() = outputCursor!!
}