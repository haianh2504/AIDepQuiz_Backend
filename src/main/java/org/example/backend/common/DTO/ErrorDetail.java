package org.example.backend.common.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        String field,
        String reason,
        Object expected,
        Object received
) {
}
