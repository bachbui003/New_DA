package com.example.ECM.service;


import com.example.ECM.model.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(Long userId);  // Sửa lại cho đúng với OrderServiceImpl
    Order getOrderById(Long id);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getAllOrders();
    Order updateOrder(Long id, Order updatedOrder);
    void deleteOrder(Long id);
}
