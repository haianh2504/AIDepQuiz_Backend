package org.example.backend.testversion.services;

import org.example.backend.testversion.DTO.QuizDefinitionResponse;
import org.example.backend.testversion.DTO.SubmitQuizRequest;
import org.example.backend.testversion.DTO.SubmitQuizResponse;

public interface AiDependenceTestService {

    QuizDefinitionResponse getAiDependenceTest();

    SubmitQuizResponse submitAiDependenceTest(SubmitQuizRequest request);
}
