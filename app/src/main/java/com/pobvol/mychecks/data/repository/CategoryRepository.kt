package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategory(category: String): CategoryEntity?
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun deleteCategory(category: CategoryEntity)

}
