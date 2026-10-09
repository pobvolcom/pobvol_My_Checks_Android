package com.pobvol.mychecks.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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

/*
table: languages
de          Deutsch
en          English
es          Español
fr          French
*/
@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey val language: String = "en",
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
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String,
    var language: String = "en", // NOT NULL
    var description: String = "",
    var category: String = "UVV",
    var icon: String?, // OPTIONAL
    var timestamp: Long = System.currentTimeMillis()
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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val checklistid: Int = 0,
    val sortno: Int = 0,
    val title: String,
    val description: String?,
    val type: String,
    val options: String?,
    val required: Boolean = true,
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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val checklistid: Int = 0,
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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val submissionid: Int = 0,
    val questionid: Int = 0,
    val value: String,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
