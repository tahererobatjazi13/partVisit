package com.partsystem.partvisitapp.feature.customer.repository

import androidx.lifecycle.LiveData
import com.partsystem.partvisitapp.core.database.dao.CustomerDao
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class CustomerRepository @Inject constructor(
    private val dao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<CustomerEntity>> = dao.getAllCustomers()

    fun getCustomerById(id: Int): LiveData<CustomerEntity> = dao.getCustomerById(id)

    suspend fun clearAll() = dao.clearAll()
}
