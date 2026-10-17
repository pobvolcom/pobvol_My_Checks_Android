package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.CategoryDAO
import com.pobvol.mychecks.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDAO
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    override suspend fun getCategory(category: String): CategoryEntity? = categoryDao.getCategory(category)
    override suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    override suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

}
