package com.prodio.production.application;

public interface SmsSender {
    void send(String phoneNumber, String message);
}
