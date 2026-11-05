package org.example.backend.common.controllers;

import org.example.backend.common.DTO.ErrorResponse;
import org.example.backend.testversion.exception.InvalidTestDefinitionException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsInvalidTestDefinitionToSanitizedInternalError() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidTestDefinition(
                new InvalidTestDefinitionException("internal configuration detail")
        );

        assertInternalError(response);
    }

    @Test
    void mapsDatabaseFailureToSanitizedInternalError() {
        ResponseEntity<ErrorResponse> response = handler.handleDataAccessException(
                new DataAccessResourceFailureException("database detail")
        );

        assertInternalError(response);
    }

    private void assertInternalError(ResponseEntity<ErrorResponse> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().error().details()).hasSize(1);
        assertThat(response.getBody().error().details().getFirst().field()).isEqualTo("errorId");
    }
}
