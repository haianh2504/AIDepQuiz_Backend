package org.example.backend.testversion.DTO;

import java.math.BigDecimal;
import java.util.List;

public record SubmitQuizResponse(
        String testSlug,
        String testVersion,
        String locale,
        OverallScore score,
        Category category,
        List<SubscaleScore> subscales,
        String feedback,
        List<String> advice,
        List<String> limitations
) {

    public record OverallScore(
            Integer totalScore,
            Integer maxTotalScore,
            BigDecimal mean,
            String displayMean
    ) {
    }

    public record Category(
            String key,
            String label,
            String description
    ) {
    }

    public record SubscaleScore(
            String key,
            String label,
            BigDecimal mean,
            String displayMean
    ) {
    }
}
