package com.prodio.production.application;

import com.prodio.production.SampleOrderCreated;
import com.prodio.production.domain.ProductionRecord;
import com.prodio.production.exception.ProductionErrorCode;
import com.prodio.production.exception.ProductionException;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductionListener {
    private final ProductionRepository repository;

    @ApplicationModuleListener
    public void handle(SampleOrderCreated orderCreated){
        if(repository.existsByOrderId(orderCreated.orderId())) throw new ProductionException(ProductionErrorCode.ALREADY_EXISTS_ORDER);
        ProductionRecord record = ProductionRecord.create(orderCreated.orderId(), orderCreated.clientPhone());
        repository.save(record);
    }
}
