package org.example.backend.question.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
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
import org.example.backend.option.entities.Option;
import org.example.backend.subscale.entities.Subscale;
import org.example.backend.testversion.entities.TestVersion;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_question_business_id",
                        columnNames = {"test_version_id", "question_id"}
                ),
                @UniqueConstraint(
                        name = "uq_question_order",
                        columnNames = {"test_version_id", "display_order"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull(message = "Test version is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_version_id", nullable = false)
    private TestVersion testVersion;

    @NotNull(message = "Subscale is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscale_id", nullable = false)
    private Subscale subscale;

    @NotBlank(message = "Question ID is required")
    @Size(max = 20, message = "Question ID must not exceed 20 characters")
    @Pattern(regexp = "^dep-[0-9]{2}$", message = "Question ID must match the format dep-XX")
    @Column(name = "question_id", nullable = false, length = 20)
    private String questionId;

    @NotBlank(message = "Question content is required")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "Question display order is required")
    @Min(value = 1, message = "Question display order must be at least 1")
    @Max(value = 16, message = "Question display order must not exceed 16")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Valid
    @OneToMany(
            mappedBy = "question",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    @Setter(AccessLevel.NONE)
    private List<Option> options = new ArrayList<>();

    @Builder
    public Question(
            TestVersion testVersion,
            Subscale subscale,
            String questionId,
            String content,
            Integer displayOrder
    ) {
        this.testVersion = testVersion;
        this.subscale = subscale;
        this.questionId = questionId;
        this.content = content;
        this.displayOrder = displayOrder;
    }

    public void addOption(Option option) {
        options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(Option option) {
        if (options.remove(option)) {
            option.setQuestion(null);
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
        Question question = (Question) other;
        return id != null && Objects.equals(id, question.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
