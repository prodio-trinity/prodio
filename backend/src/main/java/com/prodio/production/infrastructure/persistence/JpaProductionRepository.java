package com.prodio.production.infrastructure.persistence;

import com.prodio.production.application.ProductionRepository;
import com.prodio.production.application.ProductionStatus;
import com.prodio.production.domain.ProductionRecord;
import com.prodio.production.exception.ProductionErrorCode;
import com.prodio.production.exception.ProductionException;
import com.prodio.shared.PageResult;

import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaProductionRepository implements ProductionRepository {
    private final SpringDataProductionRepository springDataProductionRepository;

    @Override
    public ProductionRecord save(ProductionRecord productionRecord) {
        ProductionRecordEntity entity = ProductionRecordEntity.from(productionRecord);
        ProductionRecordEntity saved = springDataProductionRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return springDataProductionRepository.existsByOrderId(orderId);
    }

    @Override
    public ProductionRecord markShipped(Long productionId) {
        return transition(productionId, ProductionRecord::markShipped);
    }

    @Override
    public ProductionRecord markCompleted(Long productionId) {
        return transition(productionId, ProductionRecord::markCompleted);
    }

    /** id로 조회 → 넘겨받은 상태 전이(action)를 적용 → 저장, 까지의 공통 절차. */
    private ProductionRecord transition(Long productionId, UnaryOperator<ProductionRecord> action) {
        ProductionRecordEntity entity = springDataProductionRepository.findById(productionId)
                .orElseThrow(() -> new ProductionException(ProductionErrorCode.PRODUCTION_NOT_FOUND));

        ProductionRecord next = action.apply(entity.toDomain());
        springDataProductionRepository.save(ProductionRecordEntity.from(next));

        return next;
    }

    @Override
    public PageResult<ProductionRecord> findAll(ProductionStatus status, int page, int size) {
        Page<ProductionRecordEntity> result = springDataProductionRepository.search(status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        return new PageResult<>(result.getContent().stream().map(ProductionRecordEntity::toDomain).toList(),
                page, size, result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public ProductionRecord findProductionInfo(Long productionId) {
        ProductionRecordEntity entity = springDataProductionRepository.findById(productionId)
                .orElseThrow(() -> new ProductionException(ProductionErrorCode.PRODUCTION_NOT_FOUND));
        return entity.toDomain();
    }

    @Override
    public ProductionRecord addMemo(Long productionId, String note) {
        return transition(productionId, record -> record.appendMemo(note));
    }

    @Override
    public ProductionRecord clearMemo(Long productionId) {
        return transition(productionId, ProductionRecord::clearMemo);
    }
}
