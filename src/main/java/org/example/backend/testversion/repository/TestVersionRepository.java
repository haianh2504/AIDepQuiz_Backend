package org.example.backend.testversion.repository;

import org.example.backend.testversion.entities.TestVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestVersionRepository extends JpaRepository<TestVersion, Long> {

    Optional<TestVersion> findByTestSlugAndLocaleAndActiveTrue(String testSlug, String locale);
}
