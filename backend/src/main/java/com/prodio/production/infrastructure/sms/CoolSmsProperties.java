package com.prodio.production.infrastructure.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prodio.sms.coolsms")
public record CoolSmsProperties(String apiKey, String apiSecret, String senderNumber) {
}
