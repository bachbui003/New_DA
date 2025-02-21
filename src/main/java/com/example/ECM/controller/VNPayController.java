package com.example.ECM.controller;

import com.example.ECM.model.Order;
import com.example.ECM.service.OrderService;
import com.example.ECM.service.VNPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/vnpay")
public class VNPayController {
    private static final Logger logger = Logger.getLogger(VNPayController.class.getName());

    private final VNPayService vnPayService;
    private final OrderService orderService;

    public VNPayController(VNPayService vnPayService, OrderService orderService) {
        this.vnPayService = vnPayService;
        this.orderService = orderService;
    }

    // ✅ API tạo link thanh toán VNPay
    @GetMapping("/payment")
    public ResponseEntity<?> createVNPayPayment(@RequestParam Long orderId) {
        try {
            Optional<Order> orderOptional = Optional.ofNullable(orderService.getOrderById(orderId));

            if (orderOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy đơn hàng!");
            }

            Order order = orderOptional.get();
            if (order.getTotalPrice() == null || order.getTotalPrice().doubleValue() <= 0) {
                return ResponseEntity.badRequest().body("❌ Tổng giá trị đơn hàng không hợp lệ!");
            }

            String paymentUrl = vnPayService.createPaymentUrl(orderId, order.getTotalPrice().doubleValue());
            logger.info("✅ Tạo link thanh toán thành công: " + paymentUrl);
            return ResponseEntity.ok(paymentUrl);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Lỗi tạo URL thanh toán VNPay", e);
            return ResponseEntity.badRequest().body("❌ Lỗi khi tạo URL thanh toán VNPay");
        }
    }

    // ✅ API xử lý phản hồi từ VNPay
    @GetMapping("/return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {
        try {
            logger.info("✅ VNPay Callback Params: " + params);

            String txnRef = params.get("vnp_TxnRef");
            if (txnRef == null || txnRef.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy mã đơn hàng!");
            }

            Long orderId;
            try {
                orderId = Long.parseLong(txnRef);
            } catch (NumberFormatException e) {
                logger.log(Level.SEVERE, "❌ Lỗi khi chuyển đổi mã đơn hàng!", e);
                return ResponseEntity.badRequest().body("❌ Mã đơn hàng không hợp lệ!");
            }

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy đơn hàng!");
            }

            // ✅ Kiểm tra xem đơn hàng đã được thanh toán hay chưa
            if ("PAID".equals(order.getStatus())) {
                return ResponseEntity.ok("✅ Đơn hàng đã được thanh toán trước đó!");
            }

            // ✅ Xác thực giao dịch VNPay
            boolean paymentSuccess = vnPayService.handlePaymentCallback(params);
            if (paymentSuccess) {
                order.setStatus("PAID");
                orderService.updateOrder(orderId, order);
                logger.info("✅ Thanh toán thành công cho Order ID: " + orderId);
                return ResponseEntity.ok("✅ Thanh toán thành công!");
            } else {
                logger.warning("⚠ Thanh toán thất bại hoặc không hợp lệ cho Order ID: " + orderId);
                return ResponseEntity.badRequest().body("⚠ Thanh toán thất bại hoặc không hợp lệ!");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Lỗi khi xử lý phản hồi VNPay", e);
            return ResponseEntity.badRequest().body("❌ Lỗi khi xử lý phản hồi VNPay!");
        }
    }
}
