package org.example.backend.result.repository;

import org.example.backend.result.entities.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findAllByTestVersion_IdOrderByMinMeanAsc(Long testVersionId);

    Optional<Result> findByTestVersion_IdAndMinMeanLessThanEqualAndMaxMeanGreaterThanEqual(
            Long testVersionId,
            BigDecimal meanForMinRange,
            BigDecimal meanForMaxRange
    );
}
