package org.example.backend.result.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend.testversion.entities.TestVersion;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "results",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_result_category",
                columnNames = {"test_version_id", "category_key"}
        )
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull(message = "Test version is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_version_id", nullable = false)
    private TestVersion testVersion;

    @NotBlank(message = "Result category is required")
    @Pattern(regexp = "low|moderate|high", message = "Result category must be low, moderate, or high")
    @Column(name = "category_key", nullable = false, length = 20)
    private String categoryKey;

    @NotBlank(message = "Result label is required")
    @Size(max = 150, message = "Result label must not exceed 150 characters")
    @Column(name = "label", nullable = false, length = 150)
    private String label;

    @NotNull(message = "Minimum mean is required")
    @DecimalMin(value = "1.00", message = "Minimum mean must be at least 1.00")
    @DecimalMax(value = "5.00", message = "Minimum mean must not exceed 5.00")
    @Digits(integer = 1, fraction = 2, message = "Minimum mean must have up to one integer digit and two decimals")
    @Column(name = "min_mean", nullable = false, precision = 3, scale = 2)
    private BigDecimal minMean;

    @NotNull(message = "Maximum mean is required")
    @DecimalMin(value = "1.00", message = "Maximum mean must be at least 1.00")
    @DecimalMax(value = "5.00", message = "Maximum mean must not exceed 5.00")
    @Digits(integer = 1, fraction = 2, message = "Maximum mean must have up to one integer digit and two decimals")
    @Column(name = "max_mean", nullable = false, precision = 3, scale = 2)
    private BigDecimal maxMean;

    @NotBlank(message = "Result description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Result feedback is required")
    @Column(name = "feedback", nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @NotEmpty(message = "At least one advice item is required")
    @Size(max = 3, message = "Advice must contain at most three items")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "advice", nullable = false, columnDefinition = "jsonb")
    private List<String> advice = new ArrayList<>();

    @NotNull(message = "Limitations are required")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "limitations", nullable = false, columnDefinition = "jsonb")
    private List<String> limitations = new ArrayList<>();

    @Builder
    public Result(
            TestVersion testVersion,
            String categoryKey,
            String label,
            BigDecimal minMean,
            BigDecimal maxMean,
            String description,
            String feedback,
            List<String> advice,
            List<String> limitations
    ) {
        this.testVersion = testVersion;
        this.categoryKey = categoryKey;
        this.label = label;
        this.minMean = minMean;
        this.maxMean = maxMean;
        this.description = description;
        this.feedback = feedback;
        this.advice = advice == null ? new ArrayList<>() : new ArrayList<>(advice);
        this.limitations = limitations == null ? new ArrayList<>() : new ArrayList<>(limitations);
    }

    public boolean hasValidMeanRange() {
        return minMean != null && maxMean != null && minMean.compareTo(maxMean) <= 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        Result result = (Result) other;
        return id != null && Objects.equals(id, result.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
