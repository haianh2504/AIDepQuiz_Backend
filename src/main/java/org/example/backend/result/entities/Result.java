package org.example.backend.result.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "results")
@Getter
@Setter
@NoArgsConstructor
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_score", nullable = false)
    private Integer minScore;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Column(name = "level", nullable = false, length = 150)
    private String level;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evaluation", nullable = false, columnDefinition = "jsonb")
    private JsonNode evaluation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "advice", nullable = false, columnDefinition = "jsonb")
    private JsonNode advice;
}
