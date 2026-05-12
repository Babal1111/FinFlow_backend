package com.finflow.payment.controller;

import com.finflow.payment.entity.Payment;
import com.finflow.payment.service.PaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<Payment> createOrder(
            @RequestParam Long applicationId,
            @RequestHeader("X-User-Id") String userIdStr) throws RazorpayException {
        log.info("Request to create order - App: " + applicationId + ", User: " + userIdStr);
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(paymentService.createOrder(applicationId, userId));
    }

    @PostMapping("/verify")
    public ResponseEntity<Payment> verifyPayment(@RequestBody Map<String, String> data) throws RazorpayException {
        String orderId = data.get("razorpay_order_id");
        String paymentId = data.get("razorpay_payment_id");
        String signature = data.get("razorpay_signature");

        log.info("Request to verify payment - Order: " + orderId);
        return ResponseEntity.ok(paymentService.verifyPayment(orderId, paymentId, signature));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<Payment> getStatus(@PathVariable Long applicationId) {
        return paymentService.getPaymentByApplicationId(applicationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
