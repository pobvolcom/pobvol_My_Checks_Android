package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.DeviceEntity
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDeviceId(): Flow<List<DeviceEntity>>
    suspend fun insertDevice(device: DeviceEntity): Long
}
