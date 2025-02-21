package com.example.ECM.service;

import com.example.ECM.config.VNPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;

    @Autowired
    public VNPayServiceImpl(VNPayConfig vnPayConfig) {
        this.vnPayConfig = vnPayConfig;
    }

    // ✅ Tạo URL thanh toán VNPay
    @Override
    public String createPaymentUrl(Long orderId, double amount) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100))); // Chuyển từ VND sang VNPay format
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(orderId));
        vnp_Params.put("vnp_OrderInfo", "Thanh toán đơn hàng #" + orderId);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());

        // ✅ Thời gian tạo giao dịch
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(formatter));

        // ✅ Địa chỉ IP (Lấy từ request thực tế khi deploy)
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        // ✅ Sắp xếp tham số theo thứ tự từ điển
        List<String> sortedKeys = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(sortedKeys);

        // ✅ Tạo chuỗi dữ liệu để ký
        StringBuilder query = new StringBuilder();
        for (String key : sortedKeys) {
            query.append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append("=")
                    .append(URLEncoder.encode(vnp_Params.get(key), StandardCharsets.UTF_8)).append("&");
        }
        query.setLength(query.length() - 1); // Xóa ký tự `&` cuối cùng

        // ✅ Tạo chữ ký số
        String secureHash = hmacSHA512(vnPayConfig.getVnp_HashSecret(), query.toString());
        query.append("&vnp_SecureHash=").append(URLEncoder.encode(secureHash, StandardCharsets.UTF_8));

        // ✅ Trả về URL thanh toán hoàn chỉnh
        return vnPayConfig.getVnp_Url() + "?" + query.toString();
    }

    // ✅ Xác minh giao dịch khi VNPay gọi callback về
    @Override
    public boolean handlePaymentCallback(Map<String, String> vnpParams) {
        String receivedHash = vnpParams.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        // ✅ Xóa SecureHash để tạo lại chữ ký mới
        vnpParams.remove("vnp_SecureHash");

        // ✅ Sắp xếp tham số theo thứ tự từ điển
        List<String> sortedKeys = new ArrayList<>(vnpParams.keySet());
        Collections.sort(sortedKeys);

        // ✅ Tạo lại chuỗi dữ liệu để ký
        StringBuilder query = new StringBuilder();
        for (String key : sortedKeys) {
            query.append(key).append("=").append(vnpParams.get(key)).append("&");
        }
        query.setLength(query.length() - 1); // Xóa `&` cuối cùng

        // ✅ Tạo chữ ký mới
        String generatedHash = hmacSHA512(vnPayConfig.getVnp_HashSecret(), query.toString());

        // ✅ So sánh chữ ký
        return generatedHash.equalsIgnoreCase(receivedHash);
    }

    // ✅ Hàm tạo chữ ký số HMACSHA512
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // ✅ Chuyển byte[] thành HEX
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0'); // Thêm số 0 nếu cần
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase(); // ✅ Chuyển thành chữ hoa
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo HMACSHA512", e);
        }
    }
}
