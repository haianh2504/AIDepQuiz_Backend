package org.example.backend.subscale.repository;

import org.example.backend.subscale.entities.Subscale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscaleRepository extends JpaRepository<Subscale, Long> {

    List<Subscale> findAllByTestVersion_IdOrderByDisplayOrderAsc(Long testVersionId);
}
