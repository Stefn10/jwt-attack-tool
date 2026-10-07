package rs.raf.novosti.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

abstract public class PostgresAbstractRepository {

    public PostgresAbstractRepository() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected Connection newConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:postgresql://" + this.getHost() + ":" + this.getPort() + "/" + this.getDatabaseName(),
                this.getUsername(),
                this.getPassword()
        );
    }

    protected String getHost() {
        return envOr("DB_HOST", "localhost");
    }

    protected int getPort() {
        return Integer.parseInt(envOr("DB_PORT", "5432"));
    }

    protected String getDatabaseName() {
        return envOr("DB_NAME", "rafnovosti");
    }

    protected String getUsername() {
        return envOr("DB_USER", "postgres");
    }

    protected String getPassword() {
        return envOr("DB_PASSWORD", "12345678");
    }

    private static String envOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    protected void closeStatement(Statement statement) {
        try {
            Optional.of(statement).get().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected void closeResultSet(ResultSet resultSet) {
        try {
            Optional.of(resultSet).get().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected void closeConnection(Connection connection) {
        try {
            Optional.of(connection).get().close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
