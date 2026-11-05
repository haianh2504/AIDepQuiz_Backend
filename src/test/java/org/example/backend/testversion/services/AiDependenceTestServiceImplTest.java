package org.example.backend.testversion.services;

import org.example.backend.common.exception.ApiException;
import org.example.backend.option.entities.Option;
import org.example.backend.question.entities.Question;
import org.example.backend.question.repository.QuestionRepository;
import org.example.backend.result.entities.Result;
import org.example.backend.result.repository.ResultRepository;
import org.example.backend.scalelabel.entities.ScaleLabel;
import org.example.backend.scalelabel.repository.ScaleLabelRepository;
import org.example.backend.subscale.entities.Subscale;
import org.example.backend.subscale.repository.SubscaleRepository;
import org.example.backend.testversion.DTO.QuizDefinitionResponse;
import org.example.backend.testversion.DTO.SubmitQuizRequest;
import org.example.backend.testversion.DTO.SubmitQuizResponse;
import org.example.backend.testversion.entities.TestVersion;
import org.example.backend.testversion.exception.InvalidTestDefinitionException;
import org.example.backend.testversion.repository.TestVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class AiDependenceTestServiceImplTest {

    private static final Long TEST_VERSION_ID = 10L;
    private static final String TEST_VERSION = "vi-dependence-v1";

    @Mock
    private TestVersionRepository testVersionRepository;

    @Mock
    private ScaleLabelRepository scaleLabelRepository;

    @Mock
    private SubscaleRepository subscaleRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ResultRepository resultRepository;

    @InjectMocks
    private AiDependenceTestServiceImpl service;

    private TestVersion testVersion;
    private List<ScaleLabel> scaleLabels;
    private List<Subscale> subscales;
    private List<Question> questions;
    private List<Result> results;

    @BeforeEach
    void setUp() {
        testVersion = testVersion();
        scaleLabels = scaleLabels();
        subscales = subscales();
        questions = questions(subscales);
        results = results();

        lenient().when(testVersionRepository.findByTestSlugAndLocaleAndActiveTrue("ai-dependence", "vi-VN"))
                .thenReturn(Optional.of(testVersion));
    }

    @Test
    @DisplayName("given valid definition when retrieving test should map all response sections")
    void givenValidDefinition_whenGetAiDependenceTest_shouldMapAllResponseSections() {
        stubDefinitionRepositories();

        QuizDefinitionResponse response = service.getAiDependenceTest();

        assertThat(response.testSlug()).isEqualTo("ai-dependence");
        assertThat(response.testVersion()).isEqualTo(TEST_VERSION);
        assertThat(response.questionCount()).isEqualTo(16);
        assertThat(response.scale().labels()).hasSize(5);
        assertThat(response.questions()).hasSize(16);
        assertThat(response.questions().getFirst().options()).hasSize(5);
        assertThat(response.questions().getFirst().subscale()).isEqualTo("emotional");
        assertThat(response.scoring().categoryRanges()).hasSize(3);
        assertThat(response.scoring().categoryRanges().getFirst().key()).isEqualTo("low");

        verify(testVersionRepository, times(1))
                .findByTestSlugAndLocaleAndActiveTrue("ai-dependence", "vi-VN");
        verify(scaleLabelRepository, times(1))
                .findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID);
        verify(subscaleRepository, times(1))
                .findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID);
        verify(questionRepository, times(1))
                .findAllWithSubscaleAndOptionsByTestVersionId(TEST_VERSION_ID);
        verify(resultRepository, times(1))
                .findAllByTestVersion_IdOrderByMinMeanAsc(TEST_VERSION_ID);
    }

    @Test
    @DisplayName("given no active test when retrieving test should throw not found")
    void givenNoActiveTest_whenGetAiDependenceTest_shouldThrowNotFound() {
        when(testVersionRepository.findByTestSlugAndLocaleAndActiveTrue("ai-dependence", "vi-VN"))
                .thenReturn(Optional.empty());

        ApiException exception = catchApiException(service::getAiDependenceTest);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getCode()).isEqualTo("TEST_NOT_FOUND");
        verify(scaleLabelRepository, never()).findAllByTestVersion_IdOrderByDisplayOrderAsc(any());
        verify(questionRepository, never()).findAllWithSubscaleAndOptionsByTestVersionId(any());
    }

    @Test
    @DisplayName("given malformed subscale distribution when retrieving test should reject definition")
    void givenMalformedSubscaleDistribution_whenGetAiDependenceTest_shouldRejectDefinition() {
        stubDefinitionRepositories();
        List<Question> malformedQuestions = new ArrayList<>(questions);
        malformedQuestions.set(4, question("dep-05", subscales.get(0), 5, 5));
        when(questionRepository.findAllWithSubscaleAndOptionsByTestVersionId(TEST_VERSION_ID))
                .thenReturn(malformedQuestions);

        assertThatThrownBy(() -> service.getAiDependenceTest())
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Each active subscale must contain exactly four questions");
    }

    @Test
    @DisplayName("given incomplete definition when retrieving test should reject definition")
    void givenIncompleteDefinition_whenGetAiDependenceTest_shouldRejectDefinition() {
        stubDefinitionRepositories();
        when(scaleLabelRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getAiDependenceTest())
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Active test content does not satisfy the API contract");
    }

    @Test
    @DisplayName("given wrong subscale count when retrieving test should reject definition")
    void givenWrongSubscaleCount_whenGetAiDependenceTest_shouldRejectDefinition() {
        stubDefinitionRepositories();
        when(subscaleRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(subscales.subList(0, 3));

        assertThatThrownBy(() -> service.getAiDependenceTest())
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Active test content does not satisfy the API contract");
    }

    @Test
    @DisplayName("given wrong question count when retrieving test should reject definition")
    void givenWrongQuestionCount_whenGetAiDependenceTest_shouldRejectDefinition() {
        stubDefinitionRepositories();
        when(questionRepository.findAllWithSubscaleAndOptionsByTestVersionId(TEST_VERSION_ID))
                .thenReturn(questions.subList(0, 15));

        assertThatThrownBy(() -> service.getAiDependenceTest())
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Active test content does not satisfy the API contract");
    }

    @Test
    @DisplayName("given wrong result count when retrieving test should reject definition")
    void givenWrongResultCount_whenGetAiDependenceTest_shouldRejectDefinition() {
        stubDefinitionRepositories();
        when(resultRepository.findAllByTestVersion_IdOrderByMinMeanAsc(TEST_VERSION_ID))
                .thenReturn(results.subList(0, 2));

        assertThatThrownBy(() -> service.getAiDependenceTest())
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Active test content does not satisfy the API contract");
    }

    @Test
    @DisplayName("given question without five options when retrieving test should reject definition")
    void givenQuestionWithoutFiveOptions_whenGetAiDependenceTest_shouldRejectDefinition() {
        stubDefinitionRepositories();
        List<Question> malformedQuestions = new ArrayList<>(questions);
        malformedQuestions.set(0, question("dep-01", subscales.getFirst(), 1, 1));
        when(malformedQuestions.getFirst().getOptions()).thenReturn(List.of());
        when(questionRepository.findAllWithSubscaleAndOptionsByTestVersionId(TEST_VERSION_ID))
                .thenReturn(malformedQuestions);

        assertThatThrownBy(() -> service.getAiDependenceTest())
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Active test content does not satisfy the API contract");
    }

    @Test
    @DisplayName("given question without subscale when retrieving test should reject definition")
    void givenQuestionWithoutSubscale_whenGetAiDependenceTest_shouldRejectDefinition() {
        stubDefinitionRepositories();
        List<Question> malformedQuestions = new ArrayList<>(questions);
        malformedQuestions.set(0, question("dep-01", null, 1, 1));
        when(questionRepository.findAllWithSubscaleAndOptionsByTestVersionId(TEST_VERSION_ID))
                .thenReturn(malformedQuestions);

        assertThatThrownBy(() -> service.getAiDependenceTest())
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Active test content does not satisfy the API contract");
    }

    @Test
    @DisplayName("given valid answers when submitting test should calculate totals means and subscale scores")
    void givenValidAnswers_whenSubmitAiDependenceTest_shouldCalculateTotalsMeansAndSubscaleScores() {
        stubScoringRepositories();
        stubMatchingResult();
        SubmitQuizRequest request = requestWithValues(2);

        SubmitQuizResponse response = service.submitAiDependenceTest(request);

        assertThat(response.score().totalScore()).isEqualTo(32);
        assertThat(response.score().maxTotalScore()).isEqualTo(80);
        assertThat(response.score().mean()).isEqualByComparingTo("2");
        assertThat(response.score().displayMean()).isEqualTo("2.00");
        assertThat(response.category().key()).isEqualTo("low");
        assertThat(response.subscales()).hasSize(4);
        assertThat(response.subscales()).allSatisfy(score -> {
            assertThat(score.mean()).isEqualByComparingTo("2");
            assertThat(score.displayMean()).isEqualTo("2.00");
        });
        assertThat(response.advice()).containsExactly("Use AI intentionally.");
        assertThat(response.limitations()).containsExactly("Exploratory only.");

        verify(questionRepository, times(1))
                .findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID);
        verify(subscaleRepository, times(1))
                .findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID);
        verify(resultRepository, times(1))
                .findByTestVersion_IdAndMinMeanLessThanEqualAndMaxMeanGreaterThanEqual(
                        TEST_VERSION_ID, BigDecimal.valueOf(2), BigDecimal.valueOf(2)
                );
    }

    @Test
    @DisplayName("given mismatched version when submitting test should throw conflict")
    void givenMismatchedVersion_whenSubmitAiDependenceTest_shouldThrowConflict() {
        SubmitQuizRequest request = new SubmitQuizRequest("old-version", List.of());

        ApiException exception = catchApiException(() -> service.submitAiDependenceTest(request));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getCode()).isEqualTo("TEST_VERSION_MISMATCH");
        verify(questionRepository, never()).findAllByTestVersion_IdOrderByDisplayOrderAsc(any());
        verify(subscaleRepository, never()).findAllByTestVersion_IdOrderByDisplayOrderAsc(any());
    }

    @Test
    @DisplayName("given invalid question configuration when submitting test should reject definition")
    void givenInvalidQuestionConfiguration_whenSubmitAiDependenceTest_shouldRejectDefinition() {
        when(questionRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(questions.subList(0, 15));
        when(subscaleRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(subscales);

        assertThatThrownBy(() -> service.submitAiDependenceTest(requestWithValues(2)))
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Active test content does not satisfy the scoring configuration");
    }

    @Test
    @DisplayName("given wrong subscale count when submitting test should reject definition")
    void givenWrongSubscaleCount_whenSubmitAiDependenceTest_shouldRejectDefinition() {
        stubScoringRepositories();
        when(subscaleRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(subscales.subList(0, 3));

        assertThatThrownBy(() -> service.submitAiDependenceTest(requestWithValues(2)))
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Active test content does not satisfy the scoring configuration");
    }

    @Test
    @DisplayName("given question with missing subscale when submitting test should reject definition")
    void givenQuestionWithMissingSubscale_whenSubmitAiDependenceTest_shouldRejectDefinition() {
        List<Question> malformedQuestions = new ArrayList<>(questions);
        malformedQuestions.set(0, question("dep-01", null, 1, 1));
        when(questionRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(malformedQuestions);
        when(subscaleRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(subscales);

        assertThatThrownBy(() -> service.submitAiDependenceTest(requestWithValues(2)))
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("Each active subscale must contain exactly four questions");
    }

    @Test
    @DisplayName("given empty answers when submitting test should reject answer count")
    void givenEmptyAnswers_whenSubmitAiDependenceTest_shouldRejectAnswerCount() {
        stubScoringRepositories();

        ApiException exception = catchApiException(
                () -> service.submitAiDependenceTest(new SubmitQuizRequest(TEST_VERSION, List.of()))
        );

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception.getCode()).isEqualTo("INVALID_ANSWER_COUNT");
        verify(resultRepository, never())
                .findByTestVersion_IdAndMinMeanLessThanEqualAndMaxMeanGreaterThanEqual(any(), any(), any());
    }

    @Test
    @DisplayName("given duplicate question IDs when submitting test should reject answers")
    void givenDuplicateQuestionIds_whenSubmitAiDependenceTest_shouldRejectAnswers() {
        stubScoringRepositories();
        List<SubmitQuizRequest.AnswerRequest> answers = answersWithValue(2);
        answers.set(1, new SubmitQuizRequest.AnswerRequest("dep-01", 2));

        ApiException exception = catchApiException(
                () -> service.submitAiDependenceTest(new SubmitQuizRequest(TEST_VERSION, answers))
        );

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception.getCode()).isEqualTo("DUPLICATE_QUESTION_ID");
    }

    @Test
    @DisplayName("given unknown question ID when submitting test should reject answers")
    void givenUnknownQuestionId_whenSubmitAiDependenceTest_shouldRejectAnswers() {
        stubScoringRepositories();
        List<SubmitQuizRequest.AnswerRequest> answers = answersWithValue(2);
        answers.set(0, new SubmitQuizRequest.AnswerRequest("dep-99", 2));

        ApiException exception = catchApiException(
                () -> service.submitAiDependenceTest(new SubmitQuizRequest(TEST_VERSION, answers))
        );

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception.getCode()).isEqualTo("UNKNOWN_QUESTION_ID");
    }

    @ParameterizedTest(name = "invalid Likert value {0}")
    @CsvSource({"0", "6"})
    @DisplayName("given out of range Likert value when submitting test should reject answers")
    void givenOutOfRangeLikertValue_whenSubmitAiDependenceTest_shouldRejectAnswers(int value) {
        stubScoringRepositories();
        List<SubmitQuizRequest.AnswerRequest> answers = answersWithValue(2);
        answers.set(0, new SubmitQuizRequest.AnswerRequest("dep-01", value));

        ApiException exception = catchApiException(
                () -> service.submitAiDependenceTest(new SubmitQuizRequest(TEST_VERSION, answers))
        );

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception.getCode()).isEqualTo("INVALID_LIKERT_VALUE");
        assertThat(exception.getDetails()).hasSize(1);
    }

    @Test
    @DisplayName("given no matching result when submitting test should reject malformed result definition")
    void givenNoMatchingResult_whenSubmitAiDependenceTest_shouldRejectMalformedResultDefinition() {
        when(questionRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(questions);
        when(subscaleRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(subscales);
        when(resultRepository.findByTestVersion_IdAndMinMeanLessThanEqualAndMaxMeanGreaterThanEqual(
                TEST_VERSION_ID, BigDecimal.valueOf(2), BigDecimal.valueOf(2)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submitAiDependenceTest(requestWithValues(2)))
                .isInstanceOf(InvalidTestDefinitionException.class)
                .hasMessage("No result profile matches the calculated mean");
    }

    @Test
    @DisplayName("given missing subscale aggregate when calculating score should reject definition")
    void givenMissingSubscaleAggregate_whenCalculatingScore_shouldRejectDefinition()
            throws NoSuchMethodException {
        Method method = AiDependenceTestServiceImpl.class.getDeclaredMethod(
                "toSubscaleScore",
                Subscale.class,
                java.util.Map.class,
                List.class,
                TestVersion.class
        );
        method.setAccessible(true);

        assertThatThrownBy(() -> method.invoke(
                service,
                subscales.getFirst(),
                java.util.Map.of(),
                questions.subList(0, 4),
                testVersion
        ))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(InvalidTestDefinitionException.class);
    }

    @Test
    @DisplayName("given fewer questions for subscale when calculating score should reject definition")
    void givenFewerQuestionsForSubscale_whenCalculatingScore_shouldRejectDefinition()
            throws NoSuchMethodException {
        Method method = AiDependenceTestServiceImpl.class.getDeclaredMethod(
                "toSubscaleScore",
                Subscale.class,
                java.util.Map.class,
                List.class,
                TestVersion.class
        );
        method.setAccessible(true);

        assertThatThrownBy(() -> method.invoke(
                service,
                subscales.getFirst(),
                java.util.Map.of(),
                questions.subList(0, 3),
                testVersion
        ))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(InvalidTestDefinitionException.class);
    }

    @Test
    @DisplayName("given wrong subscale count when validating distribution should reject definition")
    void givenWrongSubscaleCount_whenValidatingDistribution_shouldRejectDefinition()
            throws NoSuchMethodException {
        Method method = AiDependenceTestServiceImpl.class.getDeclaredMethod(
                "validateSubscaleQuestionCounts",
                List.class,
                List.class
        );
        method.setAccessible(true);

        assertThatThrownBy(() -> method.invoke(
                service,
                subscales.subList(0, 3),
                questions
        ))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(InvalidTestDefinitionException.class);
    }

    private void stubDefinitionRepositories() {
        when(scaleLabelRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(scaleLabels);
        when(subscaleRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(subscales);
        when(questionRepository.findAllWithSubscaleAndOptionsByTestVersionId(TEST_VERSION_ID))
                .thenReturn(questions);
        when(resultRepository.findAllByTestVersion_IdOrderByMinMeanAsc(TEST_VERSION_ID))
                .thenReturn(results);
    }

    private void stubScoringRepositories() {
        when(questionRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(questions);
        when(subscaleRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(TEST_VERSION_ID))
                .thenReturn(subscales);
    }

    private void stubMatchingResult() {
        when(resultRepository.findByTestVersion_IdAndMinMeanLessThanEqualAndMaxMeanGreaterThanEqual(
                TEST_VERSION_ID, BigDecimal.valueOf(2), BigDecimal.valueOf(2)
        )).thenReturn(Optional.of(results.getFirst()));
    }

    private TestVersion testVersion() {
        TestVersion version = mock(TestVersion.class);
        lenient().when(version.getId()).thenReturn(TEST_VERSION_ID);
        lenient().when(version.getTestSlug()).thenReturn("ai-dependence");
        lenient().when(version.getTestVersion()).thenReturn(TEST_VERSION);
        lenient().when(version.getLocale()).thenReturn("vi-VN");
        lenient().when(version.getTitle()).thenReturn("AI dependence assessment");
        lenient().when(version.getQuestionCount()).thenReturn(16);
        lenient().when(version.getScaleMin()).thenReturn(1);
        lenient().when(version.getScaleMax()).thenReturn(5);
        lenient().when(version.getScaleType()).thenReturn("likert");
        lenient().when(version.getScoringMethod()).thenReturn("mean");
        lenient().when(version.getTotalMin()).thenReturn(16);
        lenient().when(version.getTotalMax()).thenReturn(80);
        lenient().when(version.getMeanMin()).thenReturn(new BigDecimal("1.00"));
        lenient().when(version.getMeanMax()).thenReturn(new BigDecimal("5.00"));
        lenient().when(version.getDisplayDecimals()).thenReturn(2);
        lenient().when(version.getReverseScoredItems()).thenReturn(false);
        return version;
    }

    private List<ScaleLabel> scaleLabels() {
        List<ScaleLabel> labels = new ArrayList<>();
        for (int value = 1; value <= 5; value++) {
            ScaleLabel label = mock(ScaleLabel.class);
            lenient().when(label.getValue()).thenReturn(value);
            lenient().when(label.getText()).thenReturn("Label " + value);
            labels.add(label);
        }
        return labels;
    }

    private List<Subscale> subscales() {
        List<Subscale> values = new ArrayList<>();
        values.add(subscale(1L, "emotional", "Phụ thuộc về cảm xúc"));
        values.add(subscale(2L, "functional", "Phụ thuộc về chức năng"));
        values.add(subscale(3L, "cognitive", "Phụ thuộc về nhận thức"));
        values.add(subscale(4L, "control", "Phụ thuộc về kiểm soát"));
        return values;
    }

    private Subscale subscale(Long id, String key, String label) {
        Subscale subscale = mock(Subscale.class);
        lenient().when(subscale.getId()).thenReturn(id);
        lenient().when(subscale.getSubscaleKey()).thenReturn(key);
        lenient().when(subscale.getLabel()).thenReturn(label);
        return subscale;
    }

    private List<Question> questions(List<Subscale> subscaleValues) {
        List<Question> values = new ArrayList<>();
        for (int index = 1; index <= 16; index++) {
            values.add(question(
                    "dep-%02d".formatted(index),
                    subscaleValues.get((index - 1) / 4),
                    index,
                    index
            ));
        }
        return values;
    }

    private Question question(String questionId, Subscale subscale, int displayOrder, int optionSeed) {
        Question question = mock(Question.class);
        lenient().when(question.getQuestionId()).thenReturn(questionId);
        lenient().when(question.getDisplayOrder()).thenReturn(displayOrder);
        lenient().when(question.getContent()).thenReturn("Question " + questionId);
        lenient().when(question.getSubscale()).thenReturn(subscale);
        List<Option> questionOptions = options(optionSeed);
        lenient().when(question.getOptions()).thenReturn(questionOptions);
        return question;
    }

    private List<Option> options(int seed) {
        List<Option> values = new ArrayList<>();
        for (int score = 1; score <= 5; score++) {
            Option option = mock(Option.class);
            lenient().when(option.getOptionId()).thenReturn("option-" + score);
            lenient().when(option.getScore()).thenReturn(score);
            lenient().when(option.getContent()).thenReturn("Option " + (seed + score));
            values.add(option);
        }
        return values;
    }

    private List<Result> results() {
        Result low = result("low", "Low", "Low description", "1.00", "2.33");
        Result moderate = result("moderate", "Moderate", "Moderate description", "2.34", "3.66");
        Result high = result("high", "High", "High description", "3.67", "5.00");
        return List.of(low, moderate, high);
    }

    private Result result(String key, String label, String description, String min, String max) {
        Result result = mock(Result.class);
        lenient().when(result.getCategoryKey()).thenReturn(key);
        lenient().when(result.getLabel()).thenReturn(label);
        lenient().when(result.getDescription()).thenReturn(description);
        lenient().when(result.getMinMean()).thenReturn(new BigDecimal(min));
        lenient().when(result.getMaxMean()).thenReturn(new BigDecimal(max));
        lenient().when(result.getFeedback()).thenReturn("Feedback");
        lenient().when(result.getAdvice()).thenReturn(List.of("Use AI intentionally."));
        lenient().when(result.getLimitations()).thenReturn(List.of("Exploratory only."));
        return result;
    }

    private SubmitQuizRequest requestWithValues(int value) {
        return new SubmitQuizRequest(TEST_VERSION, answersWithValue(value));
    }

    private List<SubmitQuizRequest.AnswerRequest> answersWithValue(int value) {
        List<SubmitQuizRequest.AnswerRequest> answers = new ArrayList<>();
        for (int index = 1; index <= 16; index++) {
            answers.add(new SubmitQuizRequest.AnswerRequest("dep-%02d".formatted(index), value));
        }
        return answers;
    }

    private ApiException catchApiException(ThrowingOperation operation) {
        try {
            operation.run();
            fail("Expected ApiException");
            return null;
        } catch (ApiException exception) {
            return exception;
        }
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run();
    }
}
