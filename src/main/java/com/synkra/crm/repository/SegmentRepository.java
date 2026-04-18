package com.synkra.crm.repository;

import com.synkra.crm.model.Contact;
import com.synkra.crm.model.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SegmentRepository extends JpaRepository<Segment, Long> {

    Optional<Segment> findByNameIgnoreCase(String name);

    @Query("""
        select distinct c
        from Segment s
        join s.contacts c
        where s.id in :segmentIds
        order by c.name asc
        """)
    List<Contact> findDistinctContactsBySegmentIds(Collection<Long> segmentIds);
}
