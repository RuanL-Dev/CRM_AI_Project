package com.synkra.crm.repository;

import com.synkra.crm.model.Contact;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("select c.status as status, count(c) as total from Contact c group by c.status")
    List<ContactStatusCountView> countGroupedByStatus();

    @Query(
        value = """
            select cast(created_at as date) as bucket_date, count(*) as total
            from contacts
            where created_at >= :start and created_at < :end
            group by cast(created_at as date)
            order by bucket_date
            """,
        nativeQuery = true
    )
    List<Object[]> countCreatedBetweenGroupedByDate(@Param("start") Instant start, @Param("end") Instant end);
}
