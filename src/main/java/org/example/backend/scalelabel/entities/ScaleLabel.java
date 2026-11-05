package org.example.backend.scalelabel.entities;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend.testversion.entities.TestVersion;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(
        name = "scale_labels",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_scale_label_value",
                        columnNames = {"test_version_id", "value"}
                ),
                @UniqueConstraint(
                        name = "uq_scale_label_order",
                        columnNames = {"test_version_id", "display_order"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScaleLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull(message = "Test version is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_version_id", nullable = false)
    private TestVersion testVersion;

    @NotNull(message = "Scale label value is required")
    @Min(value = 1, message = "Scale label value must be at least 1")
    @Max(value = 5, message = "Scale label value must not exceed 5")
    @Column(name = "value", nullable = false)
    private Integer value;

    @NotBlank(message = "Scale label text is required")
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @NotNull(message = "Scale label display order is required")
    @Min(value = 1, message = "Scale label display order must be at least 1")
    @Max(value = 5, message = "Scale label display order must not exceed 5")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    public ScaleLabel(TestVersion testVersion, Integer value, String text, Integer displayOrder) {
        this.testVersion = testVersion;
        this.value = value;
        this.text = text;
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
        ScaleLabel that = (ScaleLabel) other;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
