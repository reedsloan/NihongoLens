package com.reedsloan.nihongolens.data.repository

import com.reedsloan.nihongolens.data.local.jmdict.JMDict
import com.reedsloan.nihongolens.domain.repository.JMDictRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import okio.buffer
import okio.source
import java.io.File
import java.io.InputStream

class FakeJMDictRepository : JMDictRepository {
    // file is located in src/main/assets/jmdict-eng-common-3.5.0.json (needs to support windows and linux for CI)
    private val jmdictJsonFile = File("src${File.separator}main${File.separator}assets${File.separator}jmdict-eng-common-3.5.0.json")
    private var jmdict: JMDict? = null

    private suspend fun parseToJMDict(inputStream: InputStream): JMDict {
        return withContext(Dispatchers.IO) {
            // Create a Moshi instance
            val moshi = Moshi.Builder().build()
            val jsonAdapter: JsonAdapter<JMDict> = moshi.adapter(JMDict::class.java)

            try {
                // Reset the input stream to its initial position

                // Parse the JSON into a JMDictMoshi object
                val jmDictMoshi = jsonAdapter.fromJson(inputStream.source().buffer())
                return@withContext jmDictMoshi!!
            } catch (e: JsonDataException) {
                throw Exception("Failed to parse JMDict JSON: ${e.message}", e)
            } catch (e: IOException) {
                throw Exception("IO error while parsing JMDict JSON: ${e.message}", e)
            }
        }
    }
    override suspend fun getDictionary(): JMDict {
        if (jmdict == null) {
            jmdict = parseToJMDict(jmdictJsonFile.inputStream())
        }
        return jmdict!!
    }
}