package com.reedsloan.nihongolens.domain.use_case

import com.reedsloan.nihongolens.data.repository.FakeJMDictRepository
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishDictionary
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDictionaryTest {
    private lateinit var repository: FakeJMDictRepository
    private lateinit var getDictionary: GetDictionary
    private lateinit var japaneseEnglishDictionary: JapaneseEnglishDictionary

    @Before
     fun setUp() {
        repository = FakeJMDictRepository()
        getDictionary = GetDictionary(repository)

        runTest {
            japaneseEnglishDictionary = getDictionary.invoke()
        }
    }

    @Test
    fun `test get dictionary`() = runTest {

        // Assert that the dictionary is not empty
        assertTrue(
            "Dictionary should not be empty",
            japaneseEnglishDictionary.entries.isNotEmpty()
        )

        val lookup = japaneseEnglishDictionary.entries.find { japaneseEnglishEntry ->
            japaneseEnglishEntry.word.any { it.text == "日本" }
        }

        // Assert that the lookup result is not null
        assertNotNull("Lookup result should not be null", lookup)

        // Assert that the first result is "日本"
        assertEquals("日本", lookup!!.word.first().text)

        // Assert that the definition is "Japan"
        assertEquals("Japan", lookup.englishDefinitions.first().text.first())
    }

    @Test
    fun `test search for hashiru definition`() = runTest {
        // Search for the definition of the word "走る"
        val hashiruEntry = japaneseEnglishDictionary.entries.find { japaneseEnglishEntry ->
            japaneseEnglishEntry.word.any { it.text == "走る" }
        }

        // Assert that the entry for "走る" is not null
        assertNotNull("Definition for 走る should not be null", hashiruEntry)

        // Assert that the definition is "run"
        assertEquals("to run", hashiruEntry!!.englishDefinitions.first().text.first())

        // Assert that the second definition is "to run (of a vehicle)"
        assertEquals(
            "to run (of a vehicle)",
            hashiruEntry.englishDefinitions[1].text.first()
        )

        // assert that the part of speech is "Godan verb with ru ending"
        assertEquals(
            "Godan verb with 'ru' ending",
            hashiruEntry.englishDefinitions.first().partOfSpeech.first()
        )

        // assert that the second part of speech is Intransitive verb
        assertEquals(
            "Intransitive verb",
            hashiruEntry.englishDefinitions.first().partOfSpeech[1]
        )

        // assert definition matching "to go (e.g. bad, to extremes)" is in the english definitions
        assertTrue(hashiruEntry.englishDefinitions.any {
            it.text.contains("to go (e.g. bad, to extremes)")
        })

        println("English definitions: ${hashiruEntry.englishDefinitions}")

        // Get the misc for definition matching "to go (e.g. bad, to extremes)"
        val info =
            hashiruEntry.englishDefinitions.find {
                it.text.contains("to go (e.g. bad, to extremes)")
            }?.info?.first()

        // Assert that the info is 'as 〜に走る; occ. 趨る'
        assertEquals("as 〜に走る; occ. 趨る", info)

        // search for "って"
        val tteEntry = japaneseEnglishDictionary.entries.find { japaneseEnglishEntry ->
            japaneseEnglishEntry.wordKanaOnly.any { it.text == "って" }
        }

        // Assert that the entry for "って" is not null
        assertNotNull("Definition for って should not be null", tteEntry)

        // {"id":"2086960","kanji":[],"kana":[{"common":true,"text":"って","tags":[],"appliesToKanji":["*"]},{"common":true,"text":"て","tags":[],"appliesToKanji":["*"]}],"sense":[{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[["と",4]],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["casual quoting particle"],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"you said"},{"lang":"eng","gender":null,"type":null,"text":"he said"},{"lang":"eng","gender":null,"type":null,"text":"she said"},{"lang":"eng","gender":null,"type":null,"text":"they said"}]},{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[["たって",1]],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["after a verb in the past tense"],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"even if"}]},{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["as in かって; indicates a satirical or rhetorical question"],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"do you seriously think that"}]},{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["indicates certainty, insistence, emphasis, etc."],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"I already told you"},{"lang":"eng","gender":null,"type":null,"text":"you should know by now that"},{"lang":"eng","gender":null,"type":null,"text":"of course"}]},{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["abbr. of という"],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"the said ..."},{"lang":"eng","gender":null,"type":null,"text":"said ..."}]},{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["abbr. of と言っている"],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"says that ..."}]},{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["abbr. of と聞いている"],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"I hear that ..."}]},{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["abbr. of とは, というのは"],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"as for the term ..."}]},{"partOfSpeech":["prt"],"appliesToKanji":["*"],"appliesToKana":["*"],"related":[],"antonym":[],"field":[],"dialect":[],"misc":[],"info":["equiv. of は topic marker"],"languageSource":[],"gloss":[{"lang":"eng","gender":null,"type":null,"text":"as for ..."}]}]},
        // Assert it matches the definition "casual quoting particle"
        assertEquals("casual quoting particle", tteEntry!!.englishDefinitions.first().info.first())

        // Assert it matches the definition "you said"
        assertEquals("you said", tteEntry.englishDefinitions.first().text.first())
    }
}