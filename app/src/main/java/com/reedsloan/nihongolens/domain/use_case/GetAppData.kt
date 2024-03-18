package com.reedsloan.nihongolens.domain.use_case

import com.reedsloan.nihongolens.domain.repository.AppDataRepository

class GetAppData(
    private val repository: AppDataRepository
) {
    suspend operator fun invoke() = repository.getAppData()
}