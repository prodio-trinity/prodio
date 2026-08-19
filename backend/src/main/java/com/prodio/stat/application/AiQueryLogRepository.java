package com.prodio.stat.application;

import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.QueryType;

public interface AiQueryLogRepository {
    AiQueryLog save(AiQueryLog log);
    AiQueryLogPage findPage(long requestedBy, QueryType queryType, int page, int size);
}
