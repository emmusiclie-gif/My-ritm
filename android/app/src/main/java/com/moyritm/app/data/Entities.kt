package com.moyritm.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stick_entries",
    indices = [Index(value = ["recordedAtMillis"])]
)
data class StickEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordedAtMillis: Long,
    // Сохраняем текстом: так можно добавить собственный вкус стика.
    val stickType: String,
    val note: String? = null
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosageValue: Double,
    val dosageUnit: String,
    val note: String? = null,
    val isActive: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"])]
)
data class MedicationScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val timeMinutes: Int,
    // Биты 0–6: понедельник–воскресенье; 127 означает каждый день.
    val daysOfWeekMask: Int = 127,
    val enabled: Boolean = true
)

@Entity(
    tableName = "medication_intakes",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["medicationId"]),
        Index(value = ["scheduledAtMillis"])
    ]
)
data class MedicationIntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val scheduledAtMillis: Long,
    val takenAtMillis: Long? = null,
    // PENDING, TAKEN или SKIPPED.
    val status: String = "PENDING",
    val doseLabelSnapshot: String
)

@Entity(
    tableName = "food_products",
    indices = [Index(value = ["name"])]
)
data class FoodProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val energyKcalPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val sugarPer100g: Double,
    // LOCAL, MANUAL или IMPORTED.
    val source: String = "MANUAL"
)

@Entity(
    tableName = "food_entries",
    indices = [Index(value = ["eatenAtMillis"])]
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long? = null,
    val productNameSnapshot: String,
    val grams: Double,
    // Пищевая ценность именно съеденной порции, не на 100 г.
    val energyKcal: Double,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double,
    val sugarG: Double,
    val mealType: String,
    val eatenAtMillis: Long
)

@Entity(
    tableName = "water_entries",
    indices = [Index(value = ["recordedAtMillis"])]
)
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    // Например: «стакан», «бутылка 0,5 л».
    val containerLabel: String,
    val recordedAtMillis: Long
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val waterGoalMl: Int = 2000,
    val stickReminderEnabled: Boolean = false,
    val stickReminderIntervalMinutes: Int = 90,
    val quietHoursStartMinute: Int = 22 * 60,
    val quietHoursEndMinute: Int = 8 * 60
)
