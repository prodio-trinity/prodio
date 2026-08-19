package com.prodio.stat.application;

import com.prodio.stat.domain.AiQueryLog;
import com.prodio.stat.domain.DashboardSummary;
import com.prodio.stat.domain.ProductDistribution;
import com.prodio.stat.domain.QueryType;
import com.prodio.stat.domain.StatFilter;
import com.prodio.stat.exception.StatErrorCode;
import com.prodio.stat.exception.StatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatSummaryService {

    private final StatDashboardRepository statDashboardRepository;
    private final AiClient aiClient;
    private final AiQueryLogRepository aiQueryLogRepository;

    public AiQueryLog summarize(long adminId, StatFilter filter) {
        validate(filter);

        DashboardSummary summary = statDashboardRepository.summarize(filter);
        List<ProductDistribution> distribution = statDashboardRepository.productDistribution(filter);

        String question = describeFilter(filter);
        String response = aiClient.generateText(buildPrompt(filter, summary, distribution));

        return aiQueryLogRepository.save(AiQueryLog.summary(adminId, question, response));
    }

    public AiQueryLogPage getSummaryLogs(long adminId, int page, int size) {
        return aiQueryLogRepository.findPage(adminId, QueryType.STATS_SUMMARY, page, size);
    }

    private void validate(StatFilter filter) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new StatException(StatErrorCode.STAT_INVALID_FILTER);
        }
    }

    private String describeFilter(StatFilter filter) {
        String from = filter.from() != null ? filter.from().toString() : "전체";
        String to = filter.to() != null ? filter.to().toString() : "전체";
        String description = from + " ~ " + to;
        
        return filter.status() != null ? description + ", status=" + filter.status() : description;
    }

    private String buildPrompt(StatFilter filter, DashboardSummary summary, List<ProductDistribution> distribution) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음은 주문 통계 데이터입니다. 한국어로 자연스럽게 3~5문장으로 요약해 주세요.\n\n");
        prompt.append("조회 조건: ").append(describeFilter(filter)).append("\n");
        prompt.append("대기: ").append(summary.pendingCount()).append("건\n");
        prompt.append("생산중: ").append(summary.inProductionCount()).append("건\n");
        prompt.append("배송중: ").append(summary.inDeliveryCount()).append("건\n");
        prompt.append("완료: ").append(summary.completedCount()).append("건\n");
        prompt.append("취소: ").append(summary.cancelledCount()).append("건\n");
        prompt.append("전체: ").append(summary.totalCount()).append("건\n");
        prompt.append("완료 생산량: ").append(summary.completedQuantity()).append("\n\n");

        prompt.append("품목별 분포:\n");
        for (ProductDistribution product : distribution) {
            prompt.append("- ").append(product.productName()).append(": ")
                    .append(product.orderCount()).append("건, 수량 ").append(product.totalQuantity()).append("\n");
        }

        return prompt.toString();
    }
}
