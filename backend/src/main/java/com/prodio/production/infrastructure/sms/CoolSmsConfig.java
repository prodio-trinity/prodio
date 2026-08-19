package com.prodio.production.infrastructure.sms;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CoolSmsProperties.class)
public class CoolSmsConfig {

    @Bean
    DefaultMessageService messageService(CoolSmsProperties properties) {
        return NurigoApp.INSTANCE.initialize(
                properties.apiKey(), properties.apiSecret(), "https://api.coolsms.co.kr");
    }
}
