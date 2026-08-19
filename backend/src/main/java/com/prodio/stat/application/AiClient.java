package com.prodio.stat.application;

public interface AiClient {
    float[] embed(String text);
    String generateText(String prompt);
}
