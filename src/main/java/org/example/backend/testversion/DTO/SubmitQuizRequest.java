package org.example.backend.testversion.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public record SubmitQuizRequest(
        @NotBlank(message = "testVersion is required")
        @Size(max = 100, message = "testVersion must not exceed 100 characters")
        String testVersion,

        @NotNull(message = "answers is required")
        List<@NotNull(message = "answer must not be null") @Valid AnswerRequest> answers
) {

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record AnswerRequest(
            @NotBlank(message = "questionId is required")
            @Pattern(regexp = "^dep-[0-9]{2}$", message = "questionId must match the format dep-XX")
            String questionId,

            @NotNull(message = "value is required")
            Integer value
    ) {
    }
}
