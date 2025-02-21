package com.example.ECM.dto;

import com.example.ECM.model.Order;
import com.example.ECM.model.OrderItem;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Order order;
    private List<OrderItem> orderItems;
}
