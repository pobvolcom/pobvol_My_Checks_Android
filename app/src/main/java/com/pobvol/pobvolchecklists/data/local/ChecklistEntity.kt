package com.pobvol.pobvolchecklists.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/* table: checklists */
@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val language: String,
    val description: String,
    val category: String = "General",
    val icon: String,
    val timestamp: Long = System.currentTimeMillis()
)
