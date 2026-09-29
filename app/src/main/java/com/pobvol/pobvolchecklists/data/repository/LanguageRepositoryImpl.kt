package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.LanguageDao
import com.pobvol.pobvolchecklists.data.local.LanguageEntity
import kotlinx.coroutines.flow.Flow

class LanguageRepositoryImpl(
    private val languageDao: LanguageDao
) : LanguageRepository {
    override fun getAllLanguages(): Flow<List<LanguageEntity>> = languageDao.getAllLanguages()
    override suspend fun getLanguageByLang(lang: String): LanguageEntity? = languageDao.getLanguageByLang(lang)
    override suspend fun insertLanguage(language: LanguageEntity): Long = languageDao.insertLanguage(language)
}
