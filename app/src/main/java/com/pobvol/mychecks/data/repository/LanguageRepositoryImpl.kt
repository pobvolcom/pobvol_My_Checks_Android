package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.LanguageDAO
import com.pobvol.mychecks.data.local.LanguageEntity
import kotlinx.coroutines.flow.Flow

class LanguageRepositoryImpl(
    private val languageDao: LanguageDAO
) : LanguageRepository {
    override fun getAllLanguages(): Flow<List<LanguageEntity>> = languageDao.getAllLanguages()
    override suspend fun getLanguageByLang(lang: String): LanguageEntity? = languageDao.getLanguageByLang(lang)
    override suspend fun insertLanguage(language: LanguageEntity): Long = languageDao.insertLanguage(language)
}
