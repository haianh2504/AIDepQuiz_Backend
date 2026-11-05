package org.example.backend.option.entities;

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend.question.entities.Question;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(
        name = "options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_option_business_id",
                        columnNames = {"question_id", "option_id"}
                ),
                @UniqueConstraint(
                        name = "uq_option_question_value",
                        columnNames = {"question_id", "score"}
                ),
                @UniqueConstraint(
                        name = "uq_option_question_display_order",
                        columnNames = {"question_id", "display_order"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull(message = "Question is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @NotBlank(message = "Option ID is required")
    @Size(max = 50, message = "Option ID must not exceed 50 characters")
    @Column(name = "option_id", nullable = false, length = 50)
    private String optionId;

    @NotBlank(message = "Option content is required")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "Option score is required")
    @Min(value = 1, message = "Option score must be at least 1")
    @Max(value = 5, message = "Option score must not exceed 5")
    @Column(name = "score", nullable = false)
    private Integer score;

    @NotNull(message = "Option display order is required")
    @Min(value = 1, message = "Option display order must be at least 1")
    @Max(value = 5, message = "Option display order must not exceed 5")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    public Option(Question question, String optionId, String content, Integer score, Integer displayOrder) {
        this.question = question;
        this.optionId = optionId;
        this.content = content;
        this.score = score;
        this.displayOrder = displayOrder;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        Option option = (Option) other;
        return id != null && Objects.equals(id, option.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
