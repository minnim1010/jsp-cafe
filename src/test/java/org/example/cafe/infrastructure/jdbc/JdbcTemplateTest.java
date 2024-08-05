package org.example.cafe.infrastructure.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.example.cafe.common.exception.CafeException;
import org.example.cafe.domain.Question;
import org.example.cafe.infrastructure.database.DbConnector;
import org.example.cafe.infrastructure.jdbc.exception.JdbcTemplateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JdbcTemplate 테스트")
class JdbcTemplateTest {

    private static DbConnector dbConnector = new DbConnector().init();
    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(dbConnector.getDataSource());
    private static QuestionRowMapper questionRowMapper = new QuestionRowMapper();

    private static Long insert(String sql, int autoGeneratedKeys, Question question) {
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, autoGeneratedKeys)) {
            preparedStatement.setString(1, question.getTitle());
            preparedStatement.setString(2, question.getContent());
            preparedStatement.setString(3, question.getWriter());

            int row = preparedStatement.executeUpdate();
            if (row != 1) {
                throw new CafeException("Failed to insert question " + question.getQuestionId());
            }

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new CafeException("Cannot get generate key");
            }

            return generatedKeys.getLong(1);
        } catch (SQLException e) {
            throw new CafeException("Failed to insert question " + question.getQuestionId(), e);
        }
    }

    private static Question selectById(String sql, Long id) {
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return questionRowMapper.mapRow(resultSet, resultSet.getRow());
                }
                return null;
            }
        } catch (SQLException e) {
            throw new CafeException("Failed to find question " + id, e);
        }
    }

    private static List<Question> selectAll(String sql) {
        try (Connection connection = dbConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Question> result = new ArrayList<>();

                while (resultSet.next()) {
                    result.add(questionRowMapper.mapRow(resultSet, resultSet.getRow()));
                }

                return result;
            }
        } catch (SQLException e) {
            throw new CafeException("Failed to find questions", e);
        }
    }

    @BeforeEach
    void setUp() {
        try (Connection connection = dbConnector.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM REPLY");
            statement.execute("DELETE FROM QUESTION");
        } catch (SQLException e) {
            throw new CafeException("Failed to truncate table QUESTION", e);
        }
    }

    static class QuestionRowMapper implements RowMapper<Question> {

        @Override
        public Question mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Question(
                    rs.getLong("question_id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("writer"),
                    rs.getBoolean("is_deleted"),
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
        }
    }

    @Nested
    class 여러_데이터를_조회한다 {

        @Test
        void 여러_데이터를_조회할_수_있다() {
            // given
            final String sql = "INSERT INTO QUESTION (title, content, writer) VALUES (?, ?, ?)";
            final String selectSql = "SELECT * FROM QUESTION";
            insert(sql, Statement.RETURN_GENERATED_KEYS,
                    new Question(null, "title1", "content1", "writer1", false, null));
            insert(sql, Statement.RETURN_GENERATED_KEYS,
                    new Question(null, "title2", "content2", "writer2", false, null));
            insert(sql, Statement.RETURN_GENERATED_KEYS,
                    new Question(null, "title3", "content3", "writer3", false, null));

            // when
            List<Question> result = jdbcTemplate.query(selectSql, questionRowMapper);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(3),
                    () -> assertThat(result.get(0).getTitle()).isEqualTo("title1"),
                    () -> assertThat(result.get(1).getTitle()).isEqualTo("title2"),
                    () -> assertThat(result.get(2).getTitle()).isEqualTo("title3")
            );
        }

        @Test
        void 데이터가_존재하지_않는다면_빈_리스트를_반환한다() {
            // given
            final String selectSql = "SELECT * FROM QUESTION";

            // when
            List<Question> result = jdbcTemplate.query(selectSql, questionRowMapper);

            // then
            assertAll(
                    () -> assertThat(result).isEmpty()
            );
        }
    }

    @Nested
    class 단일_데이터를_조회한다 {

        @Test
        void 단일_데이터를_조회할_수_있다() {
            // given
            final String sql = "INSERT INTO QUESTION (title, content, writer) VALUES (?, ?, ?)";
            final String selectSql = "SELECT * FROM QUESTION WHERE question_id = ?";
            insert(sql, Statement.RETURN_GENERATED_KEYS,
                    new Question(null, "title1", "content1", "writer1", false, null));
            insert(sql, Statement.RETURN_GENERATED_KEYS,
                    new Question(null, "title2", "content2", "writer2", false, null));
            Long id = insert(sql, Statement.RETURN_GENERATED_KEYS,
                    new Question(null, "title3", "content3", "writer3", false, null));

            // when
            Question question = jdbcTemplate.queryForObject(selectSql, questionRowMapper, id);

            // then
            assertAll(
                    () -> assertThat(question).isNotNull(),
                    () -> assertThat(question.getTitle()).isEqualTo("title3")
            );
        }

        @Test
        void 데이터가_존재하지_않는다면_null을_반환한다() {
            // given
            final String selectSql = "SELECT * FROM QUESTION WHERE question_id = ?";

            // when
            Question question = jdbcTemplate.queryForObject(selectSql, questionRowMapper, 0L);

            // then
            assertAll(
                    () -> assertThat(question).isNull()
            );
        }
    }

    @Nested
    class 데이터를_삽입한다 {

        private Question result;

        @Test
        void 데이터를_삽입할_수_있다() {
            // given
            final String sql = "INSERT INTO QUESTION (title, content, writer) VALUES (?, ?, ?)";

            // when
            jdbcTemplate.update(sql, null, "title", "content", "writer");

            // then
            assertAll(
                    () -> selectAll("SELECT * FROM QUESTION").forEach(question -> {
                        assertThat(question.getTitle()).isEqualTo("title");
                        assertThat(question.getContent()).isEqualTo("content");
                        assertThat(question.getWriter()).isEqualTo("writer");
                    })
            );
        }

        @Test
        void 데이터를_삽입하고_새_데이터의_pk를_가져올_수_있다() {
            // given
            final String sql = "INSERT INTO QUESTION (title, content, writer) VALUES (?, ?, ?)";
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

            // when
            jdbcTemplate.update(sql, keyHolder, "title", "content", "writer");

            // then
            assertAll(
                    () -> {
                        assertThat(keyHolder.getKey()).isNotNull();
                        result = selectById("SELECT * FROM QUESTION WHERE question_id = ?", keyHolder.getKey());
                    },
                    () -> assertThat(result.getTitle()).isEqualTo("title"),
                    () -> assertThat(result.getContent()).isEqualTo("content"),
                    () -> assertThat(result.getWriter()).isEqualTo("writer")
            );
        }
    }

    @Nested
    class 데이터를_수정한다 {

        private Question result;

        @Test
        void 기존_데이터를_수정할_수_있다() {
            // given
            final String sql = "INSERT INTO QUESTION (title, content, writer) VALUES (?, ?, ?)";
            final String updateSql = "UPDATE QUESTION SET title = ?, content = ?, writer = ? WHERE question_id = ?";
            Long id = insert(sql, Statement.RETURN_GENERATED_KEYS,
                    new Question(null, "title", "content", "writer", false, null));

            // when
            jdbcTemplate.update(updateSql, null, "UpdatedTitle", "UpdatedContent", "UpdatedWriter", id);

            // then
            assertAll(
                    () -> {
                        result = selectById("SELECT * FROM QUESTION WHERE question_id = ?", id);
                    },
                    () -> assertThat(result.getTitle()).isEqualTo("UpdatedTitle"),
                    () -> assertThat(result.getContent()).isEqualTo("UpdatedContent"),
                    () -> assertThat(result.getWriter()).isEqualTo("UpdatedWriter")
            );
        }

        @Test
        void 존재하지_않는_데이터_수정_요청_시_예외가_발생한다() {
            // given
            final String updateSql = "UPDATE QUESTION SET title = ?, content = ?, writer = ? WHERE question_id = ?";

            // when then
            assertThrows(JdbcTemplateException.class, () -> jdbcTemplate.update(updateSql, null, 0L));
        }
    }

    @Nested
    class 데이터를_삭제한다 {

        @Test
        void 데이터를_삭제할_수_있다() {
            // given
            final String sql = "INSERT INTO QUESTION (title, content, writer) VALUES (?, ?, ?)";
            final String deleteSql = "DELETE FROM QUESTION WHERE question_id = ?";
            Long id = insert(sql, Statement.RETURN_GENERATED_KEYS,
                    new Question(null, "title", "content", "writer", false, null));

            // when
            jdbcTemplate.update(deleteSql, null, id);

            // then
            assertAll(
                    () -> assertThat(selectAll("SELECT * FROM QUESTION")).isEmpty()
            );
        }
    }
}