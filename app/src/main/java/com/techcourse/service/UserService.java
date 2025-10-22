package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id " + id));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(user, connection);
                userHistoryDao.log(new UserHistory(user, createBy), connection);

                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                    log.warn("트랜잭션 롤백 성공", e);
                } catch (SQLException ex) {
                    log.error("롤백 실패", ex);
                }
                throw new RuntimeException("비밀번호 변경 중 오류가 발생하여 롤백했습니다.", e);
            }
        } catch (SQLException e) {
            log.error("데이터베이스 연결 실패", e);
            throw new RuntimeException("데이터베이스 연결에 실패했습니다.", e);
        }
    }
}
