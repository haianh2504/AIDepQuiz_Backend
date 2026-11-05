package org.example.backend.testversion.controllers;

import jakarta.validation.Valid;
import org.example.backend.testversion.DTO.QuizDefinitionResponse;
import org.example.backend.testversion.DTO.SubmitQuizRequest;
import org.example.backend.testversion.DTO.SubmitQuizResponse;
import org.example.backend.testversion.services.AiDependenceTestService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tests")
public class AiDependenceTestController {

    private final AiDependenceTestService aiDependenceTestService;

    public AiDependenceTestController(AiDependenceTestService aiDependenceTestService) {
        this.aiDependenceTestService = aiDependenceTestService;
    }

    @GetMapping(value = "/ai-dependence", produces = MediaType.APPLICATION_JSON_VALUE)
    public QuizDefinitionResponse getAiDependenceTest() {
        return aiDependenceTestService.getAiDependenceTest();
    }

    @PostMapping(
            value = "/ai-dependence/submit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SubmitQuizResponse submitAiDependenceTest(
            @Valid @RequestBody SubmitQuizRequest request
    ) {
        return aiDependenceTestService.submitAiDependenceTest(request);
    }
}
