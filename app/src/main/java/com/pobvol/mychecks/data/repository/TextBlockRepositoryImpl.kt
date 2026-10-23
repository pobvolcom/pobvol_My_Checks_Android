package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.TextBlockDAO
import com.pobvol.mychecks.data.local.TextBlockEntity
import kotlinx.coroutines.flow.Flow

class TextBlockRepositoryImpl(
    private val textBlockDao: TextBlockDAO,
) : TextBlockRepository {
    override fun getAllTextBlocks(): Flow<List<TextBlockEntity>> = textBlockDao.getAllTextBlocks()
    override suspend fun insertTextBlock(textBlock: TextBlockEntity): Long =
        textBlockDao.insertTextBlock(textBlock)

    override suspend fun updateTextBlock(textBlock: TextBlockEntity) =
        textBlockDao.updateTextBlock(textBlock)

    override suspend fun deleteTextBlock(textBlock: TextBlockEntity) =
        textBlockDao.deleteTextBlock(textBlock)
}
