package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.TextBlockEntity
import kotlinx.coroutines.flow.Flow

interface TextBlockRepository {
    fun getAllTextBlocks(): Flow<List<TextBlockEntity>>
    suspend fun insertTextBlock(textBlock: TextBlockEntity): Long
    suspend fun updateTextBlock(textBlock: TextBlockEntity)
    suspend fun deleteTextBlock(textBlock: TextBlockEntity)
}
