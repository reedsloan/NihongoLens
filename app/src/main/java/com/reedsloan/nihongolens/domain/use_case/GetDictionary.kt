package com.reedsloan.nihongolens.domain.use_case

import com.reedsloan.nihongolens.data.local.jmdict.JMDict
import com.reedsloan.nihongolens.domain.model.EnglishDefinition
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishDictionary
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishEntry
import com.reedsloan.nihongolens.domain.model.JapaneseWord
import com.reedsloan.nihongolens.domain.repository.JMDictRepository

class GetDictionary(
    private val repository: JMDictRepository
) {
    suspend operator fun invoke(): JapaneseEnglishDictionary {
        return repository.getDictionary().toJapaneseEnglishDictionary()
    }

    private fun JMDict.toJapaneseEnglishDictionary(): JapaneseEnglishDictionary {
        val entries = this.words.map { jmDictWord ->
            val word = jmDictWord.kanji.map { kanji ->
                JapaneseWord(
                    text = kanji.text,
                    tags = kanji.tags.map { JMDict.getStringFromTag(it) },
                    common = kanji.common,
                )
            }.sortedBy { it.common }

            val kanaOnly = jmDictWord.kana.map { kana ->
                JapaneseWord(
                    text = kana.text,
                    tags = kana.tags.map { JMDict.getStringFromTag(it) },
                    common = kana.common
                )
            }.sortedBy { it.common }

            val englishDefinition = jmDictWord.sense.mapIndexed { index, sense ->
                val partOfSpeech = jmDictWord.sense[index].partOfSpeech
                val words = sense.gloss.map { it.text }
                val info = sense.info
                val misc = sense.misc
                EnglishDefinition(
                    text = words,
                    partOfSpeech = partOfSpeech.map { JMDict.getStringFromTag(it) },
                    info = info,
                    misc = misc
                )
            }

            JapaneseEnglishEntry(
                word = word.sortedBy { it.common },
                wordKanaOnly = kanaOnly.sortedBy { it.common },
                englishDefinitions = englishDefinition
            )
        }

        return JapaneseEnglishDictionary(entries)
    }
}
