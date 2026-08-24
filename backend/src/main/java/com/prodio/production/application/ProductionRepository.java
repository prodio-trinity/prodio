package com.prodio.production.application;

import com.prodio.production.domain.ProductionRecord;
import com.prodio.shared.PageResult;

public interface ProductionRepository {
    ProductionRecord save(ProductionRecord productionRecord);
    boolean existsByOrderId(Long orderId);

    ProductionRecord markShipped(Long productionId);

    ProductionRecord markCompleted(Long productionId);

    PageResult<ProductionRecord> findAll(ProductionStatus status, int page, int size);

    ProductionRecord findProductionInfoByOrderId(Long orderId);

    ProductionRecord addMemo(Long productionId, String memo);

    ProductionRecord clearMemo(Long productionId);
}
