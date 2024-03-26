package com.reedsloan.nihongolens.domain.repository

import com.reedsloan.nihongolens.data.local.jmdict.JMDict

interface JMDictRepository {
    suspend fun getDictionary(): JMDict
}
