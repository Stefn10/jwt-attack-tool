package rs.raf.novosti.repositories.user;

import rs.raf.novosti.entities.User;
import rs.raf.novosti.entities.UserType;
import rs.raf.novosti.repositories.PostgresAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresUserRepository extends PostgresAbstractRepository implements UserRepository {

    @Override
    public User findById(Long id) {
        User user = null;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();

            preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE user_id = ?");
            preparedStatement.setLong(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return user;
    }

    @Override
    public User findByEmail(String email) {
        User user = null;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();

            preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return user;
    }

    @Override
    public List<User> findAll(int page, int pageSize) {
        List<User> users = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();

            preparedStatement = connection.prepareStatement(
                "SELECT * FROM users ORDER BY user_id LIMIT ? OFFSET ?"
            );
            preparedStatement.setInt(1, pageSize);
            preparedStatement.setInt(2, (page - 1) * pageSize);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return users;
    }

    @Override
    public int count() {
        int total = 0;

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();

            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                total = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(statement);
            this.closeConnection(connection);
        }

        return total;
    }

    @Override
    public User save(User user) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();

            String[] generatedColumns = {"user_id"};

            preparedStatement = connection.prepareStatement(
                "INSERT INTO users (email, name, surname, user_type, status, password) VALUES (?, ?, ?, ?::user_type, ?, ?)",
                generatedColumns
            );
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setString(3, user.getSurname());
            preparedStatement.setString(4, user.getUserType().name());
            preparedStatement.setBoolean(5, user.isStatus());
            preparedStatement.setString(6, user.getHashedPassword());
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                user.setId(resultSet.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return user;
    }

    @Override
    public User update(User user) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();

            preparedStatement = connection.prepareStatement(
                "UPDATE users SET email = ?, name = ?, surname = ?, user_type = ?::user_type WHERE user_id = ?"
            );
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setString(3, user.getSurname());
            preparedStatement.setString(4, user.getUserType().name());
            preparedStatement.setLong(5, user.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return user;
    }

    @Override
    public void updateStatus(Long id, boolean status) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();

            preparedStatement = connection.prepareStatement(
                "UPDATE users SET status = ? WHERE user_id = ?"
            );
            preparedStatement.setBoolean(1, status);
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setName(resultSet.getString("name"));
        user.setSurname(resultSet.getString("surname"));
        user.setUserType(UserType.valueOf(resultSet.getString("user_type")));
        user.setStatus(resultSet.getBoolean("status"));
        user.setHashedPassword(resultSet.getString("password"));
        return user;
    }
}
