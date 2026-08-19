package com.prodio.production.infrastructure.sms;

import com.prodio.infra.exception.InfraErrorCode;
import com.prodio.infra.exception.InfraException;
import com.prodio.production.application.SmsSender;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoolSmsSender implements SmsSender {
    private final DefaultMessageService messageService;
    private final CoolSmsProperties coolSmsProperties;

    @Override
    public void send(String phoneNumber, String message) {
        Message coolSmsMessage = new Message();
        coolSmsMessage.setFrom(digitsOnly(coolSmsProperties.senderNumber()));
        coolSmsMessage.setTo(digitsOnly(phoneNumber));
        coolSmsMessage.setText(message);

        try {
            messageService.sendOne(new SingleMessageSendingRequest(coolSmsMessage));
        } catch (Exception e) {
            throw new InfraException(InfraErrorCode.SMS_SEND_FAILED, e);
        }
    }

    /** CoolSMS는 하이픈 없는 숫자만 된 번호를 요구해서, 어떤 형식으로 들어오든 숫자만 남긴다. */
    private static String digitsOnly(String phoneNumber) {
        return phoneNumber == null ? null : phoneNumber.replaceAll("[^0-9]", "");
    }
}
