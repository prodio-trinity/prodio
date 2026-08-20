package com.prodio.production.application;

import com.prodio.order.OrderConfirmedEvent;
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
    public void handle(OrderConfirmedEvent orderConfirmed){
        if(repository.existsByOrderId(orderConfirmed.orderId())) throw new ProductionException(ProductionErrorCode.ALREADY_EXISTS_ORDER);
        ProductionRecord record = ProductionRecord.create(orderConfirmed.orderId(), orderConfirmed.clientId(), orderConfirmed.clientContact());
        repository.save(record);
    }
}
