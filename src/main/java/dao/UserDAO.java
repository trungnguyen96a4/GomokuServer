package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends DAO {

    private static final String SELECT_USER = "SELECT * FROM user WHERE username = ? AND password = ?;";
    private static final String INSERT_USER = "INSERT INTO user (username, password) VALUES (?, ?);";
    private static final String CHECK_EXISTED = "SELECT * FROM user WHERE username = ?;";

    public User selectUser(String username, String password) {
        User user = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                user = new User(username, password);
                break;
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return user;
    }

    public boolean insertUser(String username, String password) {
        boolean flag = false;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            int result = preparedStatement.executeUpdate();
            flag = result > 0;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return flag;
    }

    public boolean checkExisted(String username, String password) {
        boolean flag = false;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_EXISTED)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                flag = true;
                break;
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return flag;
    }
}
