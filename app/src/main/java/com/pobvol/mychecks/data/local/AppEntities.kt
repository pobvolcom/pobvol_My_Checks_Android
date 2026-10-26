package com.pobvol.mychecks.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/* table: device */
@Entity(tableName = "device")
data class DeviceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val notes: String?
)

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

/*
table: languages
de          Deutsch
en          English
es          Español
fr          French
*/
@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey val language: String = "de",
    val title: String = "Deutsch"
)

/*
table: answer_types
checkbox    Checkbox
combobox    Combobox
number      Number
text        Text
NFC         NFC
qr-code     QR-Code

If answer type "combobox" is used for a question, it can also be used as status feedback.
Options: OK, minor defects, significant defects, dangerous defects

 */
@Entity(
    tableName = "answer_types",
    indices = [Index(value = ["type"], unique = true)]
)
data class AnswerTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val title: String
)

@Entity(
    tableName = "text_blocks",
)
data class TextBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sortno: Int = 0,
    val text: String
)

/*
table: categories
HEALTH      Health
UVV         UVV
*/
@Entity(
    tableName = "categories",
    indices = [Index(value = ["category"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val title: String
)


/* table: checklists */
@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val language: String = "en", // NOT NULL
    val description: String = "",
    val category: String = "UVV",
    val icon: String?, // OPTIONAL
    val timestamp: Long = System.currentTimeMillis()
)

/* table: checklist_questions */
@Entity(
    tableName = "checklist_questions",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistEntity::class,
            parentColumns = ["id"],
            childColumns = ["checklistid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["checklistid"])
    ]
)
data class ChecklistQuestionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val checklistid: String,
    val sortno: Int = 0,
    val title: String,
    val description: String?,
    val type: String,
    val options: String?,
    val required: Boolean = true,
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/* table: checklist_submissions */
@Entity(
    tableName = "checklist_submissions",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistEntity::class,
            parentColumns = ["id"],
            childColumns = ["checklistid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["checklistid"])
    ]
)
data class ChecklistSubmissionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val checklistid: String,
    val status: String,
    val inspector: String?,
    val notes: String?,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

/* table: checklist_answers */
@Entity(
    tableName = "checklist_answers",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistSubmissionEntity::class,
            parentColumns = ["id"],
            childColumns = ["submissionid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChecklistQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["submissionid"]),
        Index(value = ["questionid"])
    ]
)
data class ChecklistAnswerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val submissionid: String,
    val questionid: String,
    val value: String,
    val notes: String? = null,
    val imagePath: String? = null,
    val pictureSize: String = "medium",
    val timestamp: Long = System.currentTimeMillis()
)
