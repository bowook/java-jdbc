package com.techcourse.service;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private UserService userService;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);
        this.userHistoryDao = new UserHistoryDao(jdbcTemplate);

        this.userService = new UserService(userDao, userHistoryDao, dataSource);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        // given
        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        // when
        final var actual = userService.findById(1L);

        // then
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // given
        final var mockUserHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userServiceWithMock = new UserService(userDao, mockUserHistoryDao, dataSource);

        final var newPassword = "newPassword";
        final var createBy = "gugu";

        assertThrows(RuntimeException.class,
                () -> userServiceWithMock.changePassword(1L, newPassword, createBy));

        // when
        final var actual = userService.findById(1L);

        // then
        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
