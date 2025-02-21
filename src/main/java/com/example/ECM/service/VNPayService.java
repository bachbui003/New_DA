package com.example.ECM.service;


import java.io.UnsupportedEncodingException;
import java.util.Map;


public interface VNPayService {
        String createPaymentUrl(Long orderId, double amount) throws UnsupportedEncodingException;
    // Thêm phương thức này vào interface để tránh lỗi @Override
    boolean handlePaymentCallback(Map<String, String> vnpParams);
    }