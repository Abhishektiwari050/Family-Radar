package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relationship: String, // "Me", "Mom", "Dad", "Sister", "Brother", "Grandma", "Friend"
    val latitude: Double,
    val longitude: Double,
    val batteryPercent: Int = 100,
    val statusMessage: String = "Active",
    val isOnline: Boolean = true,
    val avatarEmoji: String = "👤",
    val colorHex: String = "#6366F1", // Default Hex
    val speed: Double = 0.0,
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val isSOS: Boolean = false
) {
    // Secondary helper constructor to pre-populate easily without Room autogenerate conflicts
    constructor(
        id: Int,
        name: String,
        relationship: String,
        latitude: Double,
        longitude: Double,
        batteryPercent: Int = 100,
        statusMessage: String = "Active",
        isOnline: Boolean = true,
        avatarEmoji: String = "👤",
        colorHex: String = "#6366F1",
        speed: Double = 0.0,
        isSOS: Boolean = false
    ) : this(
        id = id,
        name = name,
        relationship = relationship,
        latitude = latitude,
        longitude = longitude,
        batteryPercent = batteryPercent,
        statusMessage = statusMessage,
        isOnline = isOnline,
        avatarEmoji = avatarEmoji,
        colorHex = colorHex,
        speed = speed,
        lastUpdateTime = System.currentTimeMillis(),
        isSOS = isSOS
    )
}

@Entity(tableName = "geofences")
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double = 250.0,
    val emoji: String = "📍"
)

@Entity(tableName = "tracking_logs")
data class TrackingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberName: String,
    val event: String,
    val timestamp: Long = System.currentTimeMillis()
)
