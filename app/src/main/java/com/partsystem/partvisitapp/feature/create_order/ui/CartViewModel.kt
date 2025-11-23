package com.partsystem.partvisitapp.feature.create_order.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.core.database.entity.OrderEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.feature.create_order.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    val allCartItems: LiveData<List<OrderEntity>> = cartRepository.getAllCartItems()

    private val cartItems = mutableMapOf<Int, Int>()  // id → quantity

    private val _totalCount = MutableLiveData(0)
    val totalCount: LiveData<Int> = _totalCount

    fun addToCart(product: ProductEntity, quantity: Int) {
        if (quantity > 0) {
            cartItems[product.id] = quantity
        } else {
            cartItems.remove(product.id)
        }

        // محاسبه مجموع کل آیتم‌ها
        val total = cartItems.values.sum()
        _totalCount.value = total
        fun getCurrentQuantities(): Map<Int, Int> = cartItems

        viewModelScope.launch {
           val cartItem = product.name?.let {
               OrderEntity(
                   productId = product.id,
                   productName = it,
                   quantity = quantity,
                   price = 100.0
               )
           }
            if (cartItem != null) {
                cartRepository.addToCart(cartItem)
            }
        }
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, quantity)
        }
    }

    fun removeFromCart(item: OrderEntity) {
        viewModelScope.launch {
            cartRepository.removeFromCart(item)
        }
    }

    fun deleteAllCartItems() {
        viewModelScope.launch {
            cartRepository.deleteAllItems()
        }
    }
}