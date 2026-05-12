package com.finflow.payment.service;

import com.finflow.payment.entity.Payment;
import com.finflow.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.amount}")
    private Double amount;

    private RazorpayClient client;

    @PostConstruct
    public void init() throws RazorpayException {
        log.info("Initializing Razorpay Client with ID: " + razorpayKeyId);
        this.client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }

    public Payment createOrder(Long applicationId, Long userId) throws RazorpayException {
        try {
            log.info("Creating order for Application: " + applicationId + ", User: " + userId);
            
            int amountInPaise = (int) (amount * 100);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + applicationId);

            Order order = client.orders.create(orderRequest);
            log.info("Order created successfully: " + order.get("id"));

            Payment payment = Payment.builder()
                    .applicationId(applicationId)
                    .userId(userId)
                    .amount(amount)
                    .currency("INR")
                    .razorpayOrderId(order.get("id"))
                    .status(Payment.PaymentStatus.PENDING)
                    .build();

            return paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Failed to create Razorpay order: " + e.getMessage());
            throw e;
        }
    }

    public Payment verifyPayment(String orderId, String paymentId, String signature) throws RazorpayException {
        try {
            log.info("Verifying payment for Order ID: " + orderId);
            
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment record not found for Order: " + orderId));

            if (isValid) {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(paymentId);
                payment.setRazorpaySignature(signature);
                log.info("Payment verification successful for Order: " + orderId);
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                log.warn("Payment verification failed for Order: " + orderId);
            }

            return paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Error during payment verification: " + e.getMessage());
            throw e;
        }
    }

    public Optional<Payment> getPaymentByApplicationId(Long applicationId) {
        return paymentRepository.findByApplicationId(applicationId);
    }
}
