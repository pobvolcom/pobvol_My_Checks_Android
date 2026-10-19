package com.pobvol.mychecks.data.repository

import com.pobvol.mychecks.data.local.DeviceDAO
import com.pobvol.mychecks.data.local.DeviceEntity
import kotlinx.coroutines.flow.Flow

class DeviceRepositoryImpl(
    private val deviceDao: DeviceDAO
) : DeviceRepository {
    override fun getDeviceId(): Flow<List<DeviceEntity>> = deviceDao.getDeviceId()
    override suspend fun insertDevice(device: DeviceEntity): Long = deviceDao.insertDevice(device)
}
