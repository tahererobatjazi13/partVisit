package com.partsystem.partvisitapp.feature.group_product.repository

import androidx.lifecycle.LiveData
import com.partsystem.partvisitapp.core.database.dao.GroupProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupProductRepository @Inject constructor(
    private val groupProductDao: GroupProductDao,
    private val productDao: ProductDao,
) {
    fun getMainGroups(): Flow<List<GroupProductEntity>> = groupProductDao.getMainGroups()
    fun getSubGroups(parentId: Int): Flow<List<GroupProductEntity>> =
        groupProductDao.getSubGroups(parentId)

    fun getCategories(parentId: Int): Flow<List<GroupProductEntity>> =
        groupProductDao.getCategories(parentId)

    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(categoryId)

    /*  fun getProductsBySubGroup(subGroupId: Int): LiveData<List<ProductEntity>> =
          dao.getProductsBySubGroup(subGroupId)
  */
}
