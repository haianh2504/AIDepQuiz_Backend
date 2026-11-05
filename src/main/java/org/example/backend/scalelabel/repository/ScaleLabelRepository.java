package org.example.backend.scalelabel.repository;

import org.example.backend.scalelabel.entities.ScaleLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScaleLabelRepository extends JpaRepository<ScaleLabel, Long> {

    List<ScaleLabel> findAllByTestVersion_IdOrderByDisplayOrderAsc(Long testVersionId);
}
