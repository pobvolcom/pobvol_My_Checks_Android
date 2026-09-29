package com.pobvol.pobvolchecklists.data.repository

import com.pobvol.pobvolchecklists.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategory(category: String): CategoryEntity?
    suspend fun insertCategory(category: CategoryEntity): Long

}
