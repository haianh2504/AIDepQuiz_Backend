package org.example.backend.testversion.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend.question.entities.Question;
import org.example.backend.result.entities.Result;
import org.example.backend.scalelabel.entities.ScaleLabel;
import org.example.backend.subscale.entities.Subscale;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "test_versions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_test_versions_version",
                columnNames = {"test_slug", "test_version", "locale"}
        )
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotBlank(message = "Test slug is required")
    @Size(max = 100, message = "Test slug must not exceed 100 characters")
    @Pattern(regexp = "ai-dependence", message = "Test slug must be ai-dependence")
    @Column(name = "test_slug", nullable = false, length = 100)
    private String testSlug;

    @NotBlank(message = "Test version is required")
    @Size(max = 100, message = "Test version must not exceed 100 characters")
    @Column(name = "test_version", nullable = false, length = 100)
    private String testVersion;

    @NotBlank(message = "Locale is required")
    @Size(max = 10, message = "Locale must not exceed 10 characters")
    @Pattern(regexp = "vi-VN", message = "Locale must be vi-VN")
    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @NotBlank(message = "Test title is required")
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @NotNull(message = "Question count is required")
    @Min(value = 16, message = "Question count must be 16")
    @Max(value = 16, message = "Question count must be 16")
    @Column(name = "question_count", nullable = false)
    private Integer questionCount = 16;

    @NotBlank(message = "Scale type is required")
    @Size(max = 20, message = "Scale type must not exceed 20 characters")
    @Pattern(regexp = "likert", message = "Scale type must be likert")
    @Column(name = "scale_type", nullable = false, length = 20)
    private String scaleType = "likert";

    @NotNull(message = "Scale minimum is required")
    @Min(value = 1, message = "Scale minimum must be 1")
    @Max(value = 1, message = "Scale minimum must be 1")
    @Column(name = "scale_min", nullable = false)
    private Integer scaleMin = 1;

    @NotNull(message = "Scale maximum is required")
    @Min(value = 5, message = "Scale maximum must be 5")
    @Max(value = 5, message = "Scale maximum must be 5")
    @Column(name = "scale_max", nullable = false)
    private Integer scaleMax = 5;

    @NotBlank(message = "Scoring method is required")
    @Size(max = 20, message = "Scoring method must not exceed 20 characters")
    @Pattern(regexp = "mean", message = "Scoring method must be mean")
    @Column(name = "scoring_method", nullable = false, length = 20)
    private String scoringMethod = "mean";

    @NotNull(message = "Total minimum is required")
    @Min(value = 16, message = "Total minimum must be 16")
    @Max(value = 16, message = "Total minimum must be 16")
    @Column(name = "total_min", nullable = false)
    private Integer totalMin = 16;

    @NotNull(message = "Total maximum is required")
    @Min(value = 80, message = "Total maximum must be 80")
    @Max(value = 80, message = "Total maximum must be 80")
    @Column(name = "total_max", nullable = false)
    private Integer totalMax = 80;

    @NotNull(message = "Mean minimum is required")
    @DecimalMin(value = "1.00", message = "Mean minimum must be 1.00")
    @DecimalMax(value = "1.00", message = "Mean minimum must be 1.00")
    @Digits(integer = 1, fraction = 2, message = "Mean minimum must have up to one integer digit and two decimals")
    @Column(name = "mean_min", nullable = false, precision = 3, scale = 2)
    private BigDecimal meanMin = new BigDecimal("1.00");

    @NotNull(message = "Mean maximum is required")
    @DecimalMin(value = "5.00", message = "Mean maximum must be 5.00")
    @DecimalMax(value = "5.00", message = "Mean maximum must be 5.00")
    @Digits(integer = 1, fraction = 2, message = "Mean maximum must have up to one integer digit and two decimals")
    @Column(name = "mean_max", nullable = false, precision = 3, scale = 2)
    private BigDecimal meanMax = new BigDecimal("5.00");

    @NotNull(message = "Display decimals is required")
    @Min(value = 2, message = "Display decimals must be 2")
    @Max(value = 2, message = "Display decimals must be 2")
    @Column(name = "display_decimals", nullable = false)
    private Integer displayDecimals = 2;

    @NotNull(message = "Reverse scored items flag is required")
    @Column(name = "reverse_scored_items", nullable = false)
    private Boolean reverseScoredItems = false;

    @NotNull(message = "Active flag is required")
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Valid
    @OneToMany(mappedBy = "testVersion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Setter(AccessLevel.NONE)
    private List<ScaleLabel> scaleLabels = new ArrayList<>();

    @Valid
    @OneToMany(mappedBy = "testVersion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Setter(AccessLevel.NONE)
    private List<Subscale> subscales = new ArrayList<>();

    @Valid
    @OneToMany(mappedBy = "testVersion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Setter(AccessLevel.NONE)
    private List<Question> questions = new ArrayList<>();

    @Valid
    @OneToMany(mappedBy = "testVersion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("categoryKey ASC")
    @Setter(AccessLevel.NONE)
    private List<Result> results = new ArrayList<>();

    @Builder
    public TestVersion(
            String testSlug,
            String testVersion,
            String locale,
            String title,
            Integer questionCount,
            String scaleType,
            Integer scaleMin,
            Integer scaleMax,
            String scoringMethod,
            Integer totalMin,
            Integer totalMax,
            BigDecimal meanMin,
            BigDecimal meanMax,
            Integer displayDecimals,
            Boolean reverseScoredItems,
            Boolean active
    ) {
        this.testSlug = testSlug;
        this.testVersion = testVersion;
        this.locale = locale;
        this.title = title;
        this.questionCount = questionCount == null ? 16 : questionCount;
        this.scaleType = scaleType == null ? "likert" : scaleType;
        this.scaleMin = scaleMin == null ? 1 : scaleMin;
        this.scaleMax = scaleMax == null ? 5 : scaleMax;
        this.scoringMethod = scoringMethod == null ? "mean" : scoringMethod;
        this.totalMin = totalMin == null ? 16 : totalMin;
        this.totalMax = totalMax == null ? 80 : totalMax;
        this.meanMin = meanMin == null ? new BigDecimal("1.00") : meanMin;
        this.meanMax = meanMax == null ? new BigDecimal("5.00") : meanMax;
        this.displayDecimals = displayDecimals == null ? 2 : displayDecimals;
        this.reverseScoredItems = reverseScoredItems == null ? false : reverseScoredItems;
        this.active = active == null ? true : active;
    }

    public void addScaleLabel(ScaleLabel scaleLabel) {
        scaleLabels.add(scaleLabel);
        scaleLabel.setTestVersion(this);
    }

    public void removeScaleLabel(ScaleLabel scaleLabel) {
        if (scaleLabels.remove(scaleLabel)) {
            scaleLabel.setTestVersion(null);
        }
    }

    public void addSubscale(Subscale subscale) {
        subscales.add(subscale);
        subscale.setTestVersion(this);
    }

    public void removeSubscale(Subscale subscale) {
        if (subscales.remove(subscale)) {
            subscale.setTestVersion(null);
        }
    }

    public void addQuestion(Question question) {
        questions.add(question);
        question.setTestVersion(this);
    }

    public void removeQuestion(Question question) {
        if (questions.remove(question)) {
            question.setTestVersion(null);
        }
    }

    public void addResult(Result result) {
        results.add(result);
        result.setTestVersion(this);
    }

    public void removeResult(Result result) {
        if (results.remove(result)) {
            result.setTestVersion(null);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        TestVersion that = (TestVersion) other;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
