package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final String sql, final Object... params) {
        try (final Connection conn = getConnection();
             final PreparedStatement ps = prepareStatement(conn, sql, params)) {

            ps.executeUpdate();
        } catch (final SQLException e) {
            log.error("SQL 실행 실패: ", e);
            throw new RuntimeException("SQL 실행 실패: ", e);
        }
    }

    public int update(final String sql, final Object... params) {
        try (final Connection conn = getConnection();
             final PreparedStatement ps = prepareStatement(conn, sql, params)) {

            return ps.executeUpdate();
        } catch (final SQLException e) {
            log.error("SQL 실행 실패: ", e);
            throw new RuntimeException("SQL 실행 실패: ", e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> mapper, final Object... params) {
        try (final Connection conn = getConnection();
             final PreparedStatement ps = prepareStatement(conn, sql, params);
             final ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return Optional.empty();
            }
            T result = mapper.mapRowToObject(rs);
//            if (rs.next()) {
//                throw new IllegalStateException("단건 조회인데 결과가 2건 이상입니다.");
//            }
            return Optional.of(result);
        } catch (final SQLException e) {
            log.error("SQL 실행 실패: ", e);
            throw new RuntimeException("SQL 실행 실패: ", e);
        }
    }

    // 현재는 findAll에서만 사용이되고 있어서, 가변인자를 받을 필요가 없을 것 같은데, 재사용성을 위해서는 있는게 좋아보임.
    public <T> List<T> queryForList(final String sql, final RowMapper<T> mapper, final Object... params) {
        List<T> results = new ArrayList<>();

        try (final Connection conn = getConnection();
             final PreparedStatement ps = prepareStatement(conn, sql, params);
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                results.add(mapper.mapRowToObject(rs));
            }

            return results;
        } catch (final SQLException e) {
            log.error("SQL 실행 실패: ", e);
            throw new RuntimeException("SQL 실행 실패: ", e);
        }
    }


    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            log.error("DB 연결 실패: ", e);
            throw new RuntimeException("DB 연결 실패: ", e);
        }
    }

    private PreparedStatement prepareStatement(
            final Connection conn,
            final String sql,
            final Object... params) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        log.debug("query : {}", sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }

        return ps;
    }
}
