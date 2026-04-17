package com.synkra.crm.repository;

import com.synkra.crm.model.Activity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("select a.type as type, count(a) as total from Activity a group by a.type")
    List<ActivityTypeCountView> countGroupedByType();

    long countByCompletedFalseAndDueAtBefore(Instant instant);

    long countByCompletedFalseAndDueAtGreaterThanEqual(Instant instant);

    @Query(
        value = """
            select cast(created_at as date) as bucket_date, count(*) as total
            from activities
            where created_at >= :start and created_at < :end
            group by cast(created_at as date)
            order by bucket_date
            """,
        nativeQuery = true
    )
    List<Object[]> countCreatedBetweenGroupedByDate(@Param("start") Instant start, @Param("end") Instant end);
}
