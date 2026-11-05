package org.example.backend.question.repository;

import org.example.backend.question.entities.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @EntityGraph(attributePaths = "subscale")
    List<Question> findAllByTestVersion_IdOrderByDisplayOrderAsc(Long testVersionId);

    @EntityGraph(attributePaths = {"subscale", "options"})
    @Query("""
            select distinct question
            from Question question
            where question.testVersion.id = :testVersionId
            order by question.displayOrder asc
            """)
    List<Question> findAllWithSubscaleAndOptionsByTestVersionId(@Param("testVersionId") Long testVersionId);
}
