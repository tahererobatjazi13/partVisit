package com.partsystem.partvisitapp.feature.customer.repository

import androidx.lifecycle.LiveData
import com.partsystem.partvisitapp.core.database.dao.CustomerDao
import com.partsystem.partvisitapp.core.database.dao.CustomerDirectionDao
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.feature.report_factor.online.model.DirectionModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val customerDirectionDao: CustomerDirectionDao
) {

    fun getCustomerById(id: Int): LiveData<CustomerEntity> = customerDao.getCustomerByIdLive(id)

    //  کنترل برنامه ویزیت فعال
    fun getCustomersBySchedule(
        saleCenterId: Int,
        visitorId: Int,
        persianDate: String
    ): Flow<List<CustomerEntity>> =
        customerDao.getCustomersByVisitSchedule(saleCenterId, visitorId, persianDate)

    // کنترل برنامه ویزیت غیر فعال
    fun getCustomersWithoutSchedule(
        saleCenterId: Int,
        visitorId: Int
    ): Flow<List<CustomerEntity>> =
        customerDao.getCustomersWithoutVisitSchedule(saleCenterId, visitorId)


    fun getAllCustomerDirections(): Flow<List<DirectionModel>> =
        customerDirectionDao.getAllCustomerDirections()

}
