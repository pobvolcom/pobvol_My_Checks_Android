package com.pobvol.pobvolchecklists.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/* table: records */
@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)
