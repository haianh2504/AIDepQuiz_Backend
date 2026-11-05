package org.example.backend.testversion.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.common.controllers.ApiExceptionHandler;
import org.example.backend.common.exception.ApiException;
import org.example.backend.testversion.DTO.QuizDefinitionResponse;
import org.example.backend.testversion.DTO.SubmitQuizRequest;
import org.example.backend.testversion.DTO.SubmitQuizResponse;
import org.example.backend.testversion.services.AiDependenceTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiDependenceTestController.class)
@Import(ApiExceptionHandler.class)
class AiDependenceTestControllerTest {

    private static final String TEST_VERSION = "vi-dependence-v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiDependenceTestService aiDependenceTestService;

    @Test
    void givenQuizDefinition_whenGettingQuiz_shouldReturnJsonResponse() throws Exception {
        given(aiDependenceTestService.getAiDependenceTest()).willReturn(quizDefinition());

        mockMvc.perform(get("/api/v1/tests/ai-dependence"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.testSlug").value("ai-dependence"))
                .andExpect(jsonPath("$.questionCount").value(16));

        verify(aiDependenceTestService).getAiDependenceTest();
    }

    @Test
    void givenValidRequest_whenSubmittingQuiz_shouldReturnExpectedResponse() throws Exception {
        SubmitQuizRequest request = validRequest();
        given(aiDependenceTestService.submitAiDependenceTest(any(SubmitQuizRequest.class)))
                .willReturn(submitResponse());

        mockMvc.perform(post("/api/v1/tests/ai-dependence/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.score.totalScore").value(53))
                .andExpect(jsonPath("$.category.key").value("moderate"))
                .andExpect(jsonPath("$.secret").doesNotExist());

        verify(aiDependenceTestService).submitAiDependenceTest(request);
    }

    @Test
    void givenUnknownJsonProperty_whenSubmittingQuiz_shouldReturnInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tests/ai-dependence/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "testVersion": "vi-dependence-v1",
                                  "answers": [],
                                  "unexpected": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void givenInvalidRequest_whenSubmittingQuiz_shouldReturnValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/tests/ai-dependence/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "testVersion": "",
                                  "answers": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    void givenMissingTestDefinition_whenGettingQuiz_shouldReturnNotFound() throws Exception {
        given(aiDependenceTestService.getAiDependenceTest()).willThrow(new ApiException(
                HttpStatus.NOT_FOUND,
                "TEST_NOT_FOUND",
                "Không tìm thấy bài test yêu cầu.",
                List.of()
        ));

        mockMvc.perform(get("/api/v1/tests/ai-dependence"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TEST_NOT_FOUND"));
    }

    @Test
    void givenInvalidAnswers_whenSubmittingQuiz_shouldReturnUnprocessableEntity() throws Exception {
        given(aiDependenceTestService.submitAiDependenceTest(any(SubmitQuizRequest.class)))
                .willThrow(new ApiException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "INVALID_LIKERT_VALUE",
                        "Giá trị Likert phải là số nguyên từ 1 đến 5.",
                        List.of()
                ));

        mockMvc.perform(post("/api/v1/tests/ai-dependence/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_LIKERT_VALUE"));
    }

    private SubmitQuizRequest validRequest() {
        return new SubmitQuizRequest(
                TEST_VERSION,
                IntStream.rangeClosed(1, 16)
                        .mapToObj(index -> new SubmitQuizRequest.AnswerRequest(
                                "dep-%02d".formatted(index),
                                4
                        ))
                        .toList()
        );
    }

    private QuizDefinitionResponse quizDefinition() {
        return new QuizDefinitionResponse(
                "ai-dependence",
                TEST_VERSION,
                "vi-VN",
                "Mức độ phụ thuộc vào AI",
                16,
                new QuizDefinitionResponse.LikertScale(1, 5, "likert", List.of()),
                List.of(),
                new QuizDefinitionResponse.ScoringRules(
                        16,
                        "sum_and_mean",
                        new QuizDefinitionResponse.ScoreRange(16, 80),
                        new QuizDefinitionResponse.MeanRange(
                                BigDecimal.ONE,
                                BigDecimal.valueOf(5)
                        ),
                        2,
                        false,
                        List.of()
                )
        );
    }

    private SubmitQuizResponse submitResponse() {
        return new SubmitQuizResponse(
                "ai-dependence",
                TEST_VERSION,
                "vi-VN",
                new SubmitQuizResponse.OverallScore(
                        53,
                        80,
                        BigDecimal.valueOf(3.3125),
                        "3.31"
                ),
                new SubmitQuizResponse.Category(
                        "moderate",
                        "Trung bình",
                        "Mức phụ thuộc được tự báo cáo ở mức trung gian."
                ),
                List.of(),
                "Bạn đang sử dụng AI khá thường xuyên trong một số hoạt động.",
                List.of("Hãy thử tự phác thảo hướng giải quyết trước khi hỏi AI."),
                List.of("Kết quả dựa trên tự báo cáo.")
        );
    }
}
