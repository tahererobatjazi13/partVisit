package com.partsystem.partvisitapp.feature.create_order.repository

import androidx.lifecycle.LiveData
import com.partsystem.partvisitapp.core.database.dao.OrderDao
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  import com.partsystem.partvisitapp.core.database.entity.OrderEntity
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val orderDao: OrderDao
) {

    fun getAllCartItems(): LiveData<List<OrderEntity>> = orderDao.getAll()

    suspend fun addToCart(item: OrderEntity) {

        if (item.quantity <= 0) {
            orderDao.delete(item)
        } else {
            orderDao.insert(item)
        }
    }

    suspend fun updateQuantity(productId: Int, quantity: Int) {
        val item = orderDao.get(productId)
        if (item != null) {
            if (quantity == 0) {
                orderDao.delete(item)
            } else {
                orderDao.update(item.copy(quantity = quantity))
            }
        }
    }

    suspend fun removeFromCart(item: OrderEntity) {
        orderDao.delete(item)
    }

    suspend fun deleteAllItems() {
        orderDao.deleteAll()

    }

    suspend fun getProductFromCart(productId: Int): OrderEntity? {
        return orderDao.get(productId)
    }
}