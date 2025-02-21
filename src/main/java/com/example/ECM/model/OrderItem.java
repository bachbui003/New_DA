package com.example.ECM.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
@Table(name = "order_items")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private BigDecimal price;  // Giá tại thời điểm đặt hàng
}
