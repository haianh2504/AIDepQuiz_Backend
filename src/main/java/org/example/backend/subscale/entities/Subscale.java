package org.example.backend.subscale.entities;

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
import org.example.backend.question.entities.Question;
import org.example.backend.testversion.entities.TestVersion;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "subscales",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_subscale_key",
                        columnNames = {"test_version_id", "subscale_key"}
                ),
                @UniqueConstraint(
                        name = "uq_subscale_order",
                        columnNames = {"test_version_id", "display_order"}
                ),
                @UniqueConstraint(
                        name = "uq_subscale_test_version_id",
                        columnNames = {"test_version_id", "id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull(message = "Test version is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_version_id", nullable = false)
    private TestVersion testVersion;

    @NotBlank(message = "Subscale key is required")
    @Size(max = 20, message = "Subscale key must not exceed 20 characters")
    @Pattern(
            regexp = "emotional|functional|cognitive|control",
            message = "Subscale key must be emotional, functional, cognitive, or control"
    )
    @Column(name = "subscale_key", nullable = false, length = 20)
    private String subscaleKey;

    @NotBlank(message = "Subscale label is required")
    @Size(max = 150, message = "Subscale label must not exceed 150 characters")
    @Column(name = "label", nullable = false, length = 150)
    private String label;

    @NotNull(message = "Subscale display order is required")
    @Min(value = 1, message = "Subscale display order must be at least 1")
    @Max(value = 4, message = "Subscale display order must not exceed 4")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Valid
    @OneToMany(mappedBy = "subscale", fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Setter(AccessLevel.NONE)
    private List<Question> questions = new ArrayList<>();

    @Builder
    public Subscale(TestVersion testVersion, String subscaleKey, String label, Integer displayOrder) {
        this.testVersion = testVersion;
        this.subscaleKey = subscaleKey;
        this.label = label;
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
        Subscale that = (Subscale) other;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
