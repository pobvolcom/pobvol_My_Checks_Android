package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {

    fun getAllLanguages(): Flow<List<LanguageEntity>>
    suspend fun getLanguageByLang(lang: String): LanguageEntity?
    suspend fun insertLanguage(language: LanguageEntity): Long

}
