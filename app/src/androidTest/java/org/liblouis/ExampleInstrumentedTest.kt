package org.liblouis

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private lateinit var louis: Louis
    private lateinit var translationTableList: TranslationTableList

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().context
        louis = Louis(context)
        translationTableList = TranslationTableList(louis)
    }

    @Test
    fun checkVersion() {
        assertEquals(louis.getVersion(), "3.14.0")
    }

    @Test
    fun listTables() {
        val expectation = """
            [Es-Es-G0.utb, afr-za-g1.ctb, afr-za-g2.ctb, ar-ar-comp8.utb, ar-ar-g2.ctb, ar.tbl, as.tbl, awa.tbl, bg.tbl, bh.tbl, bn.tbl, bo.tbl, bra.tbl, ca.tbl, chr-us-g1.ctb, ckb.tbl, cs-comp8.utb, cs.tbl, cy-cy-g1.utb, cy.tbl, da-dk-g08.ctb, da-dk-g16-lit.ctb, da-dk-g16.ctb, da-dk-g18.ctb, da-dk-g26-lit.ctb, da-dk-g26.ctb, da-dk-g26l-lit.ctb, da-dk-g26l.ctb, da-dk-g28.ctb, da-dk-g28l.ctb, de-de-comp8.ctb, de-g0-bidi.utb, de-g0.utb, de-g1-bidi.ctb, de-g1.ctb, de-g2.ctb, dra.tbl, el.ctb, en-gb-comp8.ctb, en-gb-g1.utb, en-nabcc.utb, en-ueb-g1.ctb, en-ueb-g2.ctb, en-us-comp6.ctb, en-us-g1.ctb, en_CA.tbl, en_GB.tbl, en_US-comp8-ext.tbl, en_US.tbl, eo-g1-x-system.ctb, eo.tbl, es-g2.ctb, es.tbl, et.tbl, fa-ir-comp8.ctb, fa-ir-g1.utb, fi-fi-8dot.ctb, fi.utb, fr-bfu-comp6.utb, fr-bfu-comp8.utb, fr-bfu-g2.ctb, ga-g1.utb, ga-g2.ctb, gd.tbl, gez.tbl, gon.tbl, grc-international-en.utb, gu.tbl, haw-us-g1.ctb, he-IL-comp8.utb, he-IL.utb, he.tbl, hi.tbl, hr-comp8.tbl, hr-g1.tbl, hu-hu-comp8.ctb, hu-hu-g2.ctb, hu.tbl, hy.tbl, is.tbl, it-it-comp8.utb, it.tbl, iu-ca-g1.ctb, kha.tbl, kn.tbl, ko-2006-g1.ctb, ko-2006-g2.ctb, ko-g1.ctb, ko-g2.ctb, kok.tbl, kru.tbl, lt-6dot.tbl, lt.tbl, lv.tbl, mao-nz-g1.ctb, ml.tbl, mn-MN-g1.utb, mn-MN-g2.ctb, mni.tbl, mr.tbl, ms-my-g2.ctb, mt.tbl, mun.tbl, mwr.tbl, ne.tbl, nl-comp8.utb, nl.tbl, nl_BE.tbl, no-no-8dot-fallback-6dot-g0.utb, no-no-8dot.utb, no-no-comp8.ctb, no-no-g0.utb, no-no-g1.ctb, no-no-g2.ctb, no.tbl, or.tbl, pa.tbl, pi.tbl, pl-pl-comp8.ctb, pl.tbl, pt-pt-comp8.ctb, pt-pt-g1.utb, pt.tbl, ro.tbl, ru-compbrl.ctb, ru.ctb, ru.tbl, sa.tbl, sd.tbl, sk.tbl, sl-si-comp8.ctb, sl.tbl, sr.tbl, sv-1989.ctb, sv-1996.ctb, sv.tbl, ta-ta-g1.ctb, ta.tbl, te.tbl, tr-g2.tbl, tr.tbl, uk-comp.utb, uk.utb, ur-pk-g1.utb, ur-pk-g2.ctb, uz-g1.utb, vi.ctb, vi.tbl, zh_CHN.tbl, zh_HK.tbl, zh_TW.tbl, zhcn-g1.ctb, zhcn-g2.ctb]
            """.trimIndent()
        val tableNames = translationTableList.tables.map {
            it.fileName
        }.toString()
        assertEquals(tableNames, expectation)
    }


    // TODO: Add assertion to this one
    @Test
    fun auditTranslationTableEnumeration() {
        Log.d(TAG, "begin translation table enumeration audit")
        val translationTableList = TranslationTableList(louis)
        translationTableList.tables.forEach {
            val identifier = it.filePath
            val name = it.fileName
            val file = it.file
            if (identifier != identifier.toUpperCase()) {
                Log.d(
                    TAG,
                    "table identifier not all uppercase: $identifier"
                )
            }
            if (identifier != name.toUpperCase().replace('-', '_')) {
                Log.d(
                    TAG,
                    "table identifier doesn't match file name: $name"
                )
            }
            if (!file.exists()) {
                Log.d(TAG, "table file not found: " + file.absolutePath)
            }
        }
        Log.d(TAG, "end translation table enumeration audit")
    }

    // TODO: Make tests to test these with example values

    private fun translateText(table: TranslationTable, text: CharSequence) {
        Log.d(TAG, "begin text translation test: $text")
        val brl = louis.getBrailleTranslation(table, text)
        val braille = brl.brailleWithSpans
        Log.d(TAG, "braille translation: $braille")
        logOffsets(brl)
        val txt = louis.getTextTranslation(table, braille)
        val back = txt.textWithSpans
        Log.d(TAG, "text back-translation: $back")
        logOffsets(txt)
        Log.d(TAG, "end text translation test")
    }

    private fun translateBraille(table: TranslationTable, braille: CharSequence) {
        Log.d(TAG, "begin braille translation test: $braille")
        val txt = louis.getTextTranslation(table, braille)
        val text = txt.textWithSpans
        Log.d(TAG, "text translation: $text")
        logOffsets(txt)
        val brl = louis.getBrailleTranslation(table, text)
        val back = brl.brailleWithSpans
        Log.d(TAG, "braille back-translation: $back")
        logOffsets(brl)
        Log.d(TAG, "end braille translation test")
    }

    private fun logOffsets(translation: Translation) {
        logOutputOffsets(translation)
        logInputOffsets(translation)
    }

    private fun logOutputOffsets(translation: Translation) {
        val length = translation.inputLength
        val input = translation.consumedInput!!
        val output = translation.outputAsArray
        var from = 0
        while (from < length) {
            val to = translation.getOutputOffset(from)
            Log.d(
                TAG, String.format(
                    "in->out: %d->%d %c->%c",
                    from, to,
                    input[from], output[to]
                )
            )
            from += 1
        }
    }

    private fun logInputOffsets(translation: Translation) {
        val length = translation.outputLength
        val input = translation.consumedInput!!
        val output = translation.outputAsArray
        var from = 0
        while (from < length) {
            val to = translation.getInputOffset(from)
            Log.d(
                TAG, String.format(
                    "out->in: %d->%d %c->%c",
                    from, to,
                    output[from], input[to]
                )
            )
            from += 1
        }
    }
}
