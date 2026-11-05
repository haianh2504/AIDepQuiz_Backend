package org.example.backend.testversion.services;

import org.example.backend.common.DTO.ErrorDetail;
import org.example.backend.common.exception.ApiException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiDependenceTestServiceImpl implements AiDependenceTestService {

    private static final String TEST_SLUG = "ai-dependence";
    private static final String LOCALE = "vi-VN";
    private static final int EXPECTED_SUBSCALE_COUNT = 4;
    private static final int QUESTIONS_PER_SUBSCALE = 4;

    private final TestVersionRepository testVersionRepository;
    private final ScaleLabelRepository scaleLabelRepository;
    private final SubscaleRepository subscaleRepository;
    private final QuestionRepository questionRepository;
    private final ResultRepository resultRepository;

    public AiDependenceTestServiceImpl(
            TestVersionRepository testVersionRepository,
            ScaleLabelRepository scaleLabelRepository,
            SubscaleRepository subscaleRepository,
            QuestionRepository questionRepository,
            ResultRepository resultRepository
    ) {
        this.testVersionRepository = testVersionRepository;
        this.scaleLabelRepository = scaleLabelRepository;
        this.subscaleRepository = subscaleRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDefinitionResponse getAiDependenceTest() {
        TestVersion testVersion = findActiveTestVersion();
        List<ScaleLabel> scaleLabels = scaleLabelRepository
                .findAllByTestVersion_IdOrderByDisplayOrderAsc(testVersion.getId());
        List<Subscale> subscales = subscaleRepository
                .findAllByTestVersion_IdOrderByDisplayOrderAsc(testVersion.getId());
        List<Question> questions = questionRepository
                .findAllWithSubscaleAndOptionsByTestVersionId(testVersion.getId());
        List<Result> results = resultRepository
                .findAllByTestVersion_IdOrderByMinMeanAsc(testVersion.getId());

        validateDefinition(testVersion, scaleLabels, subscales, questions, results);

        QuizDefinitionResponse.LikertScale scale = new QuizDefinitionResponse.LikertScale(
                testVersion.getScaleMin(),
                testVersion.getScaleMax(),
                testVersion.getScaleType(),
                scaleLabels.stream()
                        .map(this::toScaleLabelResponse)
                        .toList()
        );

        List<QuizDefinitionResponse.QuestionResponse> questionResponses = questions.stream()
                .map(this::toQuestionResponse)
                .toList();

        QuizDefinitionResponse.ScoringRules scoringRules = new QuizDefinitionResponse.ScoringRules(
                testVersion.getQuestionCount(),
                testVersion.getScoringMethod(),
                new QuizDefinitionResponse.ScoreRange(testVersion.getTotalMin(), testVersion.getTotalMax()),
                new QuizDefinitionResponse.MeanRange(testVersion.getMeanMin(), testVersion.getMeanMax()),
                testVersion.getDisplayDecimals(),
                testVersion.getReverseScoredItems(),
                results.stream().map(this::toCategoryRange).toList()
        );

        return new QuizDefinitionResponse(
                testVersion.getTestSlug(),
                testVersion.getTestVersion(),
                testVersion.getLocale(),
                testVersion.getTitle(),
                testVersion.getQuestionCount(),
                scale,
                questionResponses,
                scoringRules
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SubmitQuizResponse submitAiDependenceTest(SubmitQuizRequest request) {
        TestVersion testVersion = findActiveTestVersion();

        if (!Objects.equals(testVersion.getTestVersion(), request.testVersion())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "TEST_VERSION_MISMATCH",
                    "Phiên bản bài test không còn được hỗ trợ.",
                    List.of()
            );
        }

        List<Question> questions = questionRepository
                .findAllByTestVersion_IdOrderByDisplayOrderAsc(testVersion.getId());
        List<Subscale> subscales = subscaleRepository
                .findAllByTestVersion_IdOrderByDisplayOrderAsc(testVersion.getId());

        validateQuestionConfiguration(testVersion, questions, subscales);
        Map<String, Question> questionsByBusinessId = questions.stream()
                .collect(java.util.stream.Collectors.toMap(Question::getQuestionId, question -> question));
        Map<Long, Integer> subscaleTotals = validateAndAggregateAnswers(
                request.answers(),
                questionsByBusinessId,
                testVersion
        );

        int totalScore = request.answers().stream()
                .mapToInt(SubmitQuizRequest.AnswerRequest::value)
                .sum();
        BigDecimal mean = divide(totalScore, testVersion.getQuestionCount());

        Result result = resultRepository
                .findByTestVersion_IdAndMinMeanLessThanEqualAndMaxMeanGreaterThanEqual(
                        testVersion.getId(), mean, mean
                )
                .orElseThrow(() -> new InvalidTestDefinitionException(
                        "No result profile matches the calculated mean"
                ));

        List<SubmitQuizResponse.SubscaleScore> subscaleScores = subscales.stream()
                .map(subscale -> toSubscaleScore(subscale, subscaleTotals, questions, testVersion))
                .toList();

        return new SubmitQuizResponse(
                testVersion.getTestSlug(),
                testVersion.getTestVersion(),
                testVersion.getLocale(),
                new SubmitQuizResponse.OverallScore(
                        totalScore,
                        testVersion.getTotalMax(),
                        mean,
                        formatMean(mean, testVersion.getDisplayDecimals())
                ),
                new SubmitQuizResponse.Category(
                        result.getCategoryKey(),
                        result.getLabel(),
                        result.getDescription()
                ),
                subscaleScores,
                result.getFeedback(),
                List.copyOf(result.getAdvice()),
                List.copyOf(result.getLimitations())
        );
    }

    private TestVersion findActiveTestVersion() {
        return testVersionRepository
                .findByTestSlugAndLocaleAndActiveTrue(TEST_SLUG, LOCALE)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "TEST_NOT_FOUND",
                        "Không tìm thấy bài test yêu cầu.",
                        List.of()
                ));
    }

    private void validateDefinition(
            TestVersion testVersion,
            List<ScaleLabel> scaleLabels,
            List<Subscale> subscales,
            List<Question> questions,
            List<Result> results
    ) {
        if (scaleLabels.size() != testVersion.getScaleMax() - testVersion.getScaleMin() + 1
                || subscales.size() != EXPECTED_SUBSCALE_COUNT
                || questions.size() != testVersion.getQuestionCount()
                || results.size() != 3
                || questions.stream().anyMatch(question -> question.getOptions().size() != 5
                || question.getSubscale() == null)) {
            throw new InvalidTestDefinitionException(
                    "Active test content does not satisfy the API contract"
            );
        }

        validateSubscaleQuestionCounts(subscales, questions);
    }

    private void validateQuestionConfiguration(
            TestVersion testVersion,
            List<Question> questions,
            List<Subscale> subscales
    ) {
        if (questions.size() != testVersion.getQuestionCount()
                || subscales.size() != EXPECTED_SUBSCALE_COUNT) {
            throw new InvalidTestDefinitionException(
                    "Active test content does not satisfy the scoring configuration"
            );
        }

        validateSubscaleQuestionCounts(subscales, questions);
    }

    private void validateSubscaleQuestionCounts(
            List<Subscale> subscales,
            List<Question> questions
    ) {
        Map<Long, Long> questionCountsBySubscale = questions.stream()
                .filter(question -> question.getSubscale() != null)
                .collect(Collectors.groupingBy(
                        question -> question.getSubscale().getId(),
                        Collectors.counting()
                ));

        boolean invalidDistribution = subscales.size() != EXPECTED_SUBSCALE_COUNT
                || subscales.stream().anyMatch(subscale ->
                questionCountsBySubscale.getOrDefault(subscale.getId(), 0L)
                        != QUESTIONS_PER_SUBSCALE
        );

        if (invalidDistribution) {
            throw new InvalidTestDefinitionException(
                    "Each active subscale must contain exactly four questions"
            );
        }
    }

    private Map<Long, Integer> validateAndAggregateAnswers(
            List<SubmitQuizRequest.AnswerRequest> answers,
            Map<String, Question> questionsByBusinessId,
            TestVersion testVersion
    ) {
        if (answers.size() != testVersion.getQuestionCount()) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "INVALID_ANSWER_COUNT",
                    "Bài test yêu cầu đủ 16 câu trả lời.",
                    List.of(new ErrorDetail(
                            "answers",
                            "Expected 16 answers.",
                            testVersion.getQuestionCount(),
                            answers.size()
                    ))
            );
        }

        Set<String> seenQuestionIds = new HashSet<>();
        Map<Long, Integer> subscaleTotals = new HashMap<>();

        for (int answerIndex = 0; answerIndex < answers.size(); answerIndex++) {
            SubmitQuizRequest.AnswerRequest answer = answers.get(answerIndex);
            if (!seenQuestionIds.add(answer.questionId())) {
                throw new ApiException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "DUPLICATE_QUESTION_ID",
                        "Danh sách câu trả lời không được chứa questionId trùng nhau.",
                        List.of()
                );
            }

            Question question = questionsByBusinessId.get(answer.questionId());
            if (question == null) {
                throw new ApiException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "UNKNOWN_QUESTION_ID",
                        "questionId không thuộc phiên bản bài test.",
                        List.of()
                );
            }

            if (answer.value() < testVersion.getScaleMin() || answer.value() > testVersion.getScaleMax()) {
                throw new ApiException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "INVALID_LIKERT_VALUE",
                        "Giá trị Likert phải là số nguyên từ 1 đến 5.",
                        List.of(new ErrorDetail(
                                "answers[" + answerIndex + "].value",
                                "Value must be an integer from 1 to 5.",
                                "1..5",
                                answer.value()
                        ))
                );
            }

            Long subscaleId = question.getSubscale().getId();
            subscaleTotals.merge(subscaleId, answer.value(), Integer::sum);
        }

        return subscaleTotals;
    }

    private QuizDefinitionResponse.ScaleLabelResponse toScaleLabelResponse(ScaleLabel scaleLabel) {
        return new QuizDefinitionResponse.ScaleLabelResponse(scaleLabel.getValue(), scaleLabel.getText());
    }

    private QuizDefinitionResponse.QuestionResponse toQuestionResponse(Question question) {
        return new QuizDefinitionResponse.QuestionResponse(
                question.getQuestionId(),
                question.getDisplayOrder(),
                question.getSubscale().getSubscaleKey(),
                question.getSubscale().getLabel(),
                question.getContent(),
                question.getOptions().stream()
                        .map(option -> new QuizDefinitionResponse.LikertOption(
                                option.getOptionId(),
                                option.getScore(),
                                option.getContent()
                        ))
                        .toList()
        );
    }

    private QuizDefinitionResponse.CategoryRange toCategoryRange(Result result) {
        return new QuizDefinitionResponse.CategoryRange(
                result.getCategoryKey(),
                result.getLabel(),
                result.getMinMean(),
                result.getMaxMean()
        );
    }

    private SubmitQuizResponse.SubscaleScore toSubscaleScore(
            Subscale subscale,
            Map<Long, Integer> subscaleTotals,
            List<Question> questions,
            TestVersion testVersion
    ) {
        long questionCount = questions.stream()
                .filter(question -> Objects.equals(question.getSubscale().getId(), subscale.getId()))
                .count();
        Integer total = subscaleTotals.get(subscale.getId());

        if (questionCount != QUESTIONS_PER_SUBSCALE || total == null) {
            throw new InvalidTestDefinitionException(
                    "Each subscale must contain four answered questions"
            );
        }

        BigDecimal mean = divide(total, Math.toIntExact(questionCount));
        return new SubmitQuizResponse.SubscaleScore(
                subscale.getSubscaleKey(),
                subscale.getLabel(),
                mean,
                formatMean(mean, testVersion.getDisplayDecimals())
        );
    }

    private BigDecimal divide(int numerator, int denominator) {
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 10, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    private String formatMean(BigDecimal mean, int decimals) {
        return mean.setScale(decimals, RoundingMode.HALF_UP).toPlainString();
    }
}
