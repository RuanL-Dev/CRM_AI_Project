package com.synkra.crm.repository;

import com.synkra.crm.model.Deal;
import com.synkra.crm.model.DealStage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {

    @Query("select d.stage as stage, coalesce(sum(d.value), 0) as total from Deal d group by d.stage")
    List<DealStageValueView> sumValueGroupedByStage();

    @Query("select coalesce(sum(d.value), 0) from Deal d where d.stage <> :stage")
    BigDecimal sumValueWhereStageNot(@Param("stage") DealStage stage);

    @Query("select coalesce(sum(d.value), 0) from Deal d where d.stage = :stage")
    BigDecimal sumValueWhereStage(@Param("stage") DealStage stage);

    @Query(
        value = """
            select cast(created_at as date) as bucket_date, count(*) as total
            from deals
            where created_at >= :start and created_at < :end
            group by cast(created_at as date)
            order by bucket_date
            """,
        nativeQuery = true
    )
    List<Object[]> countCreatedBetweenGroupedByDate(@Param("start") Instant start, @Param("end") Instant end);
}
