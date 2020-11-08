package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {
    protected String jdbcURL = "jdbc:mysql://localhost:3306/mmt?allowPublicKeyRetrieval=true&useSSL=false";
    protected String jdbcUsername = "root";
    protected String jdbcPassword = "12345678";

    public DAO() {

    }

    protected Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }

}
