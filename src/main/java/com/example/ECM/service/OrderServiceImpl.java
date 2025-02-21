package com.example.ECM.service;

import com.example.ECM.model.Cart;
import com.example.ECM.model.Order;
import com.example.ECM.model.User;
import com.example.ECM.repository.CartRepository;
import com.example.ECM.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public Order createOrder(Long userId) {  // Đã sửa lại đúng với interface
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng!");
        }

        Order newOrder = new Order();
        newOrder.setUser(cart.getUser()); // Phải đảm bảo phương thức setUser() tồn tại
        newOrder.setTotalPrice(BigDecimal.valueOf(
                cart.getCartItems().stream()
                        .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                        .sum()
        ));
        newOrder.setStatus("PENDING");

        Order savedOrder = orderRepository.save(newOrder);

        // Xóa giỏ hàng sau khi đặt hàng
        cart.clearItems();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order updateOrder(Long id, Order updatedOrder) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(updatedOrder.getStatus());
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setStatus(status);
        return orderRepository.save(order);
    }


    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found");
        }
        orderRepository.deleteById(id);
    }
}
