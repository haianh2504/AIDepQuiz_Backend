package org.example.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import org.example.backend.question.entities.Question;
import org.example.backend.question.repository.QuestionRepository;
import org.example.backend.result.entities.Result;
import org.example.backend.result.repository.ResultRepository;
import org.example.backend.testversion.repository.TestVersionRepository;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.jpa.properties.hibernate.generate_statistics=true"
)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = BackendApplicationTests.PostgresInitializer.class)
class BackendApplicationTests {

    private static final String TEST_VERSION = "vi-dependence-v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestVersionRepository testVersionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @AfterAll
    static void stopPostgres() {
        PostgresInitializer.stop();
    }

    @Test
    void contextLoadsWithPostgresAndValidatedSchema() {
        assertThat(testVersionRepository.findByTestSlugAndLocaleAndActiveTrue(
                "ai-dependence", "vi-VN"
        )).isPresent();
    }

    @Test
    void derivedQuestionRepositoryQueryExecutesAgainstPostgres() {
        Long testVersionId = testVersionRepository
                .findByTestSlugAndLocaleAndActiveTrue("ai-dependence", "vi-VN")
                .orElseThrow()
                .getId();

        assertThat(questionRepository.findAllByTestVersion_IdOrderByDisplayOrderAsc(testVersionId))
                .hasSize(16);
    }

    @Test
    void getQuizReturnsContractPayload() throws Exception {
        mockMvc.perform(get("/api/v1/tests/ai-dependence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testSlug").value("ai-dependence"))
                .andExpect(jsonPath("$.testVersion").value(TEST_VERSION))
                .andExpect(jsonPath("$.questionCount").value(16))
                .andExpect(jsonPath("$.questions", hasSize(16)))
                .andExpect(jsonPath("$.questions[12].subscaleLabel")
                        .value("Phụ thuộc về kiểm soát"))
                .andExpect(jsonPath("$.scoring.categoryRanges", hasSize(3)));
    }

    @Test
    void submitRejectsUnknownJsonProperties() throws Exception {
        mockMvc.perform(post("/api/v1/tests/ai-dependence/submit")
                        .contentType("application/json")
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

    @ParameterizedTest(name = "total score {0} maps to {1}")
    @CsvSource({
            "37, low",
            "38, moderate",
            "58, moderate",
            "59, high"
    })
    void submitUsesUnroundedMeanAtCategoryBoundaries(
            int totalScore,
            String expectedCategory
    ) throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "testVersion", TEST_VERSION,
                "answers", answersForTotal(totalScore)
        ));

        mockMvc.perform(post("/api/v1/tests/ai-dependence/submit")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.key").value(expectedCategory));
    }

    @ParameterizedTest(name = "mean {0} maps to {1}")
    @CsvSource({
            "2.33, low",
            "2.34, moderate",
            "3.66, moderate",
            "3.67, high"
    })
    void resultRepositoryExecutesInclusiveRangeQuery(
            String meanValue,
            String expectedCategory
    ) {
        Long testVersionId = testVersionRepository
                .findByTestSlugAndLocaleAndActiveTrue("ai-dependence", "vi-VN")
                .orElseThrow()
                .getId();
        BigDecimal mean = new BigDecimal(meanValue);

        Result result = resultRepository
                .findByTestVersion_IdAndMinMeanLessThanEqualAndMaxMeanGreaterThanEqual(
                        testVersionId, mean, mean
                )
                .orElseThrow();

        assertThat(result.getCategoryKey()).isEqualTo(expectedCategory);
    }

    @Test
    @Transactional
    void questionEntityGraphAvoidsPerQuestionAssociationQueries() {
        Long testVersionId = testVersionRepository
                .findByTestSlugAndLocaleAndActiveTrue("ai-dependence", "vi-VN")
                .orElseThrow()
                .getId();
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        List<Question> questions = questionRepository
                .findAllWithSubscaleAndOptionsByTestVersionId(testVersionId);

        questions.forEach(question -> {
            assertThat(Hibernate.isInitialized(question.getSubscale())).isTrue();
            assertThat(Hibernate.isInitialized(question.getOptions())).isTrue();
            question.getSubscale().getLabel();
            question.getOptions().size();
        });

        assertThat(questions).hasSize(16);
        assertThat(statistics.getPrepareStatementCount()).isLessThan(questions.size());
    }

    private List<Map<String, Object>> answersForTotal(int totalScore) {
        int remaining = totalScore - 16;
        List<Map<String, Object>> answers = new ArrayList<>();

        for (int index = 1; index <= 16; index++) {
            int increment = Math.min(4, remaining);
            answers.add(Map.of(
                    "questionId", "dep-%02d".formatted(index),
                    "value", 1 + increment
            ));
            remaining -= increment;
        }

        assertThat(remaining).isZero();
        return answers;
    }

    static final class PostgresInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private static final String MIGRATION_PATH =
                "supabase/migrations/20261102084017_database_schema_ai_dep_quiz.sql";

        private static final PostgreSQLContainer<?> POSTGRES = startPostgres();

        private static PostgreSQLContainer<?> startPostgres() {
            PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("aidpquiz")
                    .withUsername("test")
                    .withPassword("test");
            container.start();

            // Migrate before Spring creates the EntityManagerFactory so ddl-auto=validate checks PostgreSQL.
            DriverManagerDataSource dataSource = new DriverManagerDataSource(
                    container.getJdbcUrl(),
                    container.getUsername(),
                    container.getPassword()
            );
            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(
                        connection,
                        new EncodedResource(
                                new FileSystemResource(MIGRATION_PATH),
                                StandardCharsets.UTF_8
                        )
                );
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to initialize the PostgreSQL test schema", exception);
            }
            return container;
        }

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES.getUsername(),
                    "spring.datasource.password=" + POSTGRES.getPassword(),
                    "spring.jpa.hibernate.ddl-auto=validate"
            ).applyTo(context.getEnvironment());
        }

        static void stop() {
            if (POSTGRES.isRunning()) {
                POSTGRES.stop();
            }
        }
    }
}
