package com.prodio.production.application;

import com.prodio.production.domain.ProductionRecord;

public interface ProductionRepository {
    ProductionRecord save(ProductionRecord productionRecord);
    boolean existsByOrderId(Long orderId);
}
