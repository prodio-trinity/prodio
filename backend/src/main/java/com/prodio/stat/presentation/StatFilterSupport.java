package com.prodio.stat.presentation;

import com.prodio.stat.domain.OrderViewStatus;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;

final class StatFilterSupport {

    private StatFilterSupport() {}

    static OrderViewStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OrderViewStatus.from(value);
        } catch (IllegalArgumentException exception) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }
}
