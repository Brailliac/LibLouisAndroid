package org.liblouis

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import java.util.*
import kotlin.experimental.or

open class Translation(builder: TranslationBuilder, backTranslate: Boolean) {
    private external fun translate(
        tableName: String,
        inputBuffer: String, outputBuffer: CharArray, typeForm: ByteArray?,
        outputOffsets: IntArray, inputOffsets: IntArray,
        resultValues: IntArray, backTranslate: Boolean
    ): Boolean

    val translationTable: TranslationTable? = builder.translationTable
    private val translationSucceeded: Boolean
    val suppliedInput: CharSequence?
    val consumedInput: CharSequence?
    private val inputOffsets: IntArray
    val inputCursor: Int?
    val outputAsArray: CharArray
    private var outputString: String? = null
    var outputWithSpans: CharSequence? = null
        get() {
            synchronized(this) {
                if (field == null) {
                    val sb = SpannableStringBuilder(outputAsString)
                    copyInputSpans(sb)
                    field = sb.subSequence(0, sb.length)
                }
            }
            return field
        }
        private set
    private val outputOffsets: IntArray
    val outputCursor: Int?

    val inputLength: Int
        get() = consumedInput!!.length

    fun getInputOffset(outputOffset: Int): Int {
        return if (outputOffset == outputLength) inputLength else inputOffsets[outputOffset]
    }

    fun findFirstInputOffset(inputOffset: Int): Int {
        var inputOffset = inputOffset
        val outputOffset = getOutputOffset(inputOffset)
        while (inputOffset > 0) {
            val next = inputOffset - 1
            if (getOutputOffset(next) != outputOffset) break
            inputOffset = next
        }
        return inputOffset
    }

    fun findLastInputOffset(inputOffset: Int): Int {
        var inputOffset = inputOffset
        val outputOffset = getOutputOffset(inputOffset)
        val inputLength = inputLength
        while (inputOffset < inputLength) {
            val next = inputOffset + 1
            if (getOutputOffset(next) != outputOffset) break
            inputOffset = next
        }
        return inputOffset
    }

    val outputAsString: String?
        get() {
            synchronized(this) {
                if (outputString == null) {
                    outputString = String(outputAsArray)
                }
            }
            return outputString
        }

    private fun copyInputSpans(sb: SpannableStringBuilder) {
        val input = consumedInput
        if (input is Spanned) {
            val spanned = input
            val spans =
                spanned.getSpans(0, spanned.length, Any::class.java)
            if (spans != null) {
                for (span in spans) {
                    val start = getOutputOffset(spanned.getSpanStart(span))
                    val end = getOutputOffset(spanned.getSpanEnd(span))
                    val flags = spanned.getSpanFlags(span)
                    sb.setSpan(span, start, end, flags)
                }
            }
        }
    }

    val outputLength: Int
        get() = outputAsArray.size

    fun getOutputOffset(inputOffset: Int): Int {
        return if (inputOffset == inputLength) outputLength else outputOffsets[inputOffset]
    }

    fun findFirstOutputOffset(outputOffset: Int): Int {
        var outputOffset = outputOffset
        val inputOffset = getInputOffset(outputOffset)
        while (outputOffset > 0) {
            val next = outputOffset - 1
            if (getInputOffset(next) != inputOffset) break
            outputOffset = next
        }
        return outputOffset
    }

    fun findLastOutputOffset(outputOffset: Int): Int {
        var outputOffset = outputOffset
        val inputOffset = getInputOffset(outputOffset)
        val outputLength = outputLength
        while (outputOffset < outputLength) {
            val next = outputOffset + 1
            if (getInputOffset(next) != inputOffset) break
            outputOffset = next
        }
        return outputOffset
    }

    private enum class ResultValuesIndex {
        INPUT_LENGTH, OUTPUT_LENGTH, CURSOR_OFFSET
        // end of enumeration
    }

    companion object {
        private val LOG_TAG = Translation::class.java.name
        private const val TYPE_FORM_ITALIC: Byte = 0X1
        private const val TYPE_FORM_UNDERLINE: Byte = 0X2
        private const val TYPE_FORM_BOLD: Byte = 0X4
        private const val TYPE_FORM_COMPUTER: Byte = 0X8
        private fun createTypeForm(length: Int): ByteArray {
            val typeForm = ByteArray(length)
            Arrays.fill(typeForm, 0.toByte())
            return typeForm
        }

        private fun createTypeForm(length: Int, text: CharSequence?): ByteArray? {
            var typeForm: ByteArray? = null
            if (text is Spanned) {
                val spanned = text
                val spans =
                    spanned.getSpans(0, spanned.length, Any::class.java)
                if (spans != null) {
                    for (span in spans) {
                        var flags: Byte = 0
                        if (span is UnderlineSpan) {
                            flags = TYPE_FORM_UNDERLINE
                        } else if (span is StyleSpan) {
                            when (span.style) {
                                Typeface.BOLD -> flags = TYPE_FORM_BOLD
                                Typeface.ITALIC -> flags = TYPE_FORM_ITALIC
                                Typeface.BOLD_ITALIC -> flags =
                                    TYPE_FORM_BOLD or TYPE_FORM_ITALIC
                            }
                        }
                        if (flags.toInt() != 0) {
                            if (typeForm == null) typeForm =
                                createTypeForm(length)
                            val start = spanned.getSpanStart(span)
                            val end = spanned.getSpanEnd(span)
                            var index = start
                            while (index < end) {
                                typeForm[index] = typeForm[index] or flags
                                index += 1
                            }
                        }
                    }
                }
            }
            return typeForm
        }

        private val RESULT_VALUES_COUNT =
            ResultValuesIndex.values().size
        private val RVI_INPUT_LENGTH = ResultValuesIndex.INPUT_LENGTH.ordinal
        private val RVI_OUTPUT_LENGTH = ResultValuesIndex.OUTPUT_LENGTH.ordinal
        private val RVI_CURSOR_OFFSET = ResultValuesIndex.CURSOR_OFFSET.ordinal
    }

    init {
        suppliedInput = builder.inputCharacters
        inputCursor = builder.cursorOffset
        val includeHighlighting = builder.includeHighlighting
        val allowLongerOutput = builder.allowLongerOutput
        var outputLength = builder.outputLength
        val inputString = suppliedInput.toString()
        val inputLength = inputString.length
        var input = suppliedInput
        var outOffsets = IntArray(inputLength)
        val resultValues =
            IntArray(RESULT_VALUES_COUNT)
        var output: CharArray
        var inOffsets: IntArray
        var translated: Boolean
        while (true) {
            output = CharArray(outputLength)
            inOffsets = IntArray(outputLength)
            resultValues[RVI_INPUT_LENGTH] = inputLength
            resultValues[RVI_OUTPUT_LENGTH] = outputLength
            resultValues[RVI_CURSOR_OFFSET] =
                inputCursor ?: -1
            val typeFormLength = Math.max(inputLength, outputLength)
            val typeForm =
                if (!includeHighlighting) null else if (backTranslate) null else createTypeForm(
                    typeFormLength,
                    input
                )
            synchronized(Louis.NATIVE_LOCK) {
                translated = translate(
                    translationTable!!.fileName,
                    inputString, output, typeForm,
                    outOffsets, inOffsets, resultValues,
                    backTranslate
                )
            }
            if (!translated) {
                Log.w(LOG_TAG, "translation failed")
                if (resultValues[RVI_INPUT_LENGTH] > resultValues[RVI_OUTPUT_LENGTH]
                ) {
                    resultValues[RVI_INPUT_LENGTH] =
                        resultValues[RVI_OUTPUT_LENGTH]
                } else if (resultValues[RVI_OUTPUT_LENGTH] > resultValues[RVI_INPUT_LENGTH]
                ) {
                    resultValues[RVI_OUTPUT_LENGTH] =
                        resultValues[RVI_INPUT_LENGTH]
                }
                var offset = 0
                while (offset < resultValues[RVI_INPUT_LENGTH]) {
                    outOffsets[offset] = offset
                    inOffsets[offset] = outOffsets[offset]
                    offset += 1
                }
                if (resultValues[RVI_CURSOR_OFFSET] >= resultValues[RVI_INPUT_LENGTH]
                ) {
                    resultValues[RVI_CURSOR_OFFSET] = -1
                }
                inputString.toCharArray(output, 0, 0, inputString.length)
                break
            }
            if (resultValues[RVI_INPUT_LENGTH] == inputLength) break
            if (!allowLongerOutput) break
            outputLength = outputLength shl 1
        }
        val newInputLength = resultValues[RVI_INPUT_LENGTH]
        val newOutputLength = resultValues[RVI_OUTPUT_LENGTH]
        val newCursorOffset = resultValues[RVI_CURSOR_OFFSET]
        if (newInputLength < inputLength) {
            input = input!!.subSequence(0, newInputLength)
            outOffsets = Arrays.copyOf(outOffsets, newInputLength)
        }
        if (newOutputLength < outputLength) {
            output = Arrays.copyOf(output, newOutputLength)
            inOffsets = Arrays.copyOf(inOffsets, newOutputLength)
        }
        consumedInput = input
        outputAsArray = output
        outputOffsets = outOffsets
        inputOffsets = inOffsets
        outputCursor =
            if (newCursorOffset < 0) null else Integer.valueOf(newCursorOffset)
        translationSucceeded = translated
    }
}