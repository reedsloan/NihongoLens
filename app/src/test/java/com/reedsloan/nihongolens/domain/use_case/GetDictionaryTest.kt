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
    }
}