package com.prodio.catalog.infrastructure.persistence;

import com.prodio.catalog.CatalogOrderLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCatalogOrderLookupRepository implements CatalogOrderLookup {
    private final SpringDataCatalogClientRepository springDataCatalogClientRepository;
    private final SpringDataCatalogProductRepository springDataCatalogProductRepository;

    @Override
    public Optional<ClientSnapshot> findClient(long clientId) {
        return springDataCatalogClientRepository.findById(clientId)
                .map(JpaCatalogOrderLookupRepository::toClientSnapshot);
    }

    @Override
    public Optional<ClientSnapshot> findClientByAccountId(long accountId) {
        return springDataCatalogClientRepository.findByUserId(accountId)
                .map(JpaCatalogOrderLookupRepository::toClientSnapshot);
    }

    @Override
    public Optional<ProductSnapshot> findProduct(long productId) {
        return springDataCatalogProductRepository.findById(productId)
                .map(JpaCatalogOrderLookupRepository::toProductSnapshot);
    }

    @Override
    public List<ProductSnapshot> findActiveProducts() {
        return springDataCatalogProductRepository.findAllByActiveTrueOrderByProductNameAsc().stream()
                .map(JpaCatalogOrderLookupRepository::toProductSnapshot)
                .toList();
    }

    private static ClientSnapshot toClientSnapshot(CatalogClientEntity entity) {
        return new ClientSnapshot(entity.getId(), entity.getClientCode(), entity.getCompanyName(),
                entity.getCeoName(), entity.getBusinessRegNo(), entity.getAddress(), entity.getPhone(),
                entity.getManagerName(), entity.getMemo(), entity.isActive());
    }

    private static ProductSnapshot toProductSnapshot(CatalogProductEntity entity) {
        long unitPrice = entity.getUnitPrice().setScale(0, RoundingMode.HALF_UP).longValueExact();
        return new ProductSnapshot(entity.getId(), entity.getProductCode(), entity.getProductName(),
                entity.getSubCategoryId(), entity.getUnit().name(), entity.getDescription(),
                entity.getMemo(), unitPrice, entity.isActive());
    }
}
