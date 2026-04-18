package com.synkra.crm.repository;

import com.synkra.crm.model.FormDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormDefinitionRepository extends JpaRepository<FormDefinition, Long> {
    Optional<FormDefinition> findBySlug(String slug);
}
