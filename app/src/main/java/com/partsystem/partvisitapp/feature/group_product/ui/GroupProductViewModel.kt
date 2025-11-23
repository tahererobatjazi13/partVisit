package com.partsystem.partvisitapp.feature.group_product.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.feature.group_product.repository.GroupProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupProductViewModel @Inject constructor(
    private val repository: GroupProductRepository
) : ViewModel() {

    val mainGroupList: LiveData<List<GroupProductEntity>> = repository.getMainGroups().asLiveData()

    fun getSubGroups(id: Int): LiveData<List<GroupProductEntity>> =
        repository.getSubGroups(id).asLiveData()

    fun getCategories(id: Int): LiveData<List<GroupProductEntity>> =
        repository.getCategories(id).asLiveData()

    fun getProductsByCategory(categoryId: Int): LiveData<List<ProductEntity>> {
        return repository.getProductsByCategory(categoryId).asLiveData()
    }

    /* fun getProductsBySubGroup(subGroupId: Int): LiveData<List<ProductEntity>> {
         return repository.getProductsBySubGroup(subGroupId)
     }*/

}
