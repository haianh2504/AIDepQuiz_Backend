package org.example.backend.testversion.DTO;

import java.math.BigDecimal;
import java.util.List;

public record QuizDefinitionResponse(
        String testSlug,
        String testVersion,
        String locale,
        String title,
        Integer questionCount,
        LikertScale scale,
        List<QuestionResponse> questions,
        ScoringRules scoring
) {

    public record LikertScale(
            Integer min,
            Integer max,
            String type,
            List<ScaleLabelResponse> labels
    ) {
    }

    public record ScaleLabelResponse(
            Integer value,
            String text
    ) {
    }

    public record QuestionResponse(
            String questionId,
            Integer questionNumber,
            String subscale,
            String subscaleLabel,
            String text,
            List<LikertOption> options
    ) {
    }

    public record LikertOption(
            String optionId,
            Integer value,
            String text
    ) {
    }

    public record ScoringRules(
            Integer itemCount,
            String scoringMethod,
            ScoreRange totalRange,
            MeanRange meanRange,
            Integer displayDecimals,
            Boolean reverseScoredItems,
            List<CategoryRange> categoryRanges
    ) {
    }

    public record ScoreRange(
            Integer min,
            Integer max
    ) {
    }

    public record MeanRange(
            BigDecimal min,
            BigDecimal max
    ) {
    }

    public record CategoryRange(
            String key,
            String label,
            BigDecimal minMean,
            BigDecimal maxMean
    ) {
    }
}
