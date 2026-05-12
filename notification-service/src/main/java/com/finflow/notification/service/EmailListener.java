package com.finflow.notification.service;

import com.finflow.common.event.NotificationEvent;
import com.finflow.notification.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveNotification(NotificationEvent event) {
        log.info("Received notification event: {}", event);

        if (event.getEmail() == null || event.getEmail().isEmpty()) {
            log.warn("Email address is missing for user {}. Cannot send email.", event.getUserId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Optional: you can set your from address or rely on properties
            // message.setFrom("support@finflow.com");
            message.setTo(event.getEmail());
            message.setSubject(event.getSubject());
            message.setText(event.getMessage());

            mailSender.send(message);
            log.info("Email sent successfully to {} for event type {}", event.getEmail(), event.getType());
            
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", event.getEmail(), e.getMessage());
            // If you throw exception here, RabbitMQ will typically requeue the message or send to DLQ (if configured).
            // For now, gracefully catching so it doesn't infinitely loop on auth error.
        }
    }
}
