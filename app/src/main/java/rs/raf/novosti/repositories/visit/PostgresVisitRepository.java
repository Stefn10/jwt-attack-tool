package rs.raf.novosti.repositories.visit;

import rs.raf.novosti.repositories.PostgresAbstractRepository;

import java.sql.*;

public class PostgresVisitRepository extends PostgresAbstractRepository implements VisitRepository {

    @Override
    public boolean addVisit(Long newsId, String sessionId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "INSERT INTO news_visits (news_id, session_id) VALUES (?, ?) ON CONFLICT DO NOTHING"
            );
            preparedStatement.setLong(1, newsId);
            preparedStatement.setString(2, sessionId);
            int rows = preparedStatement.executeUpdate();
            return rows > 0; // true if it is a new visit
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return false;
    }

    @Override
    public boolean hasVisited(Long newsId, String sessionId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT 1 FROM news_visits WHERE news_id = ? AND session_id = ?"
            );
            preparedStatement.setLong(1, newsId);
            preparedStatement.setString(2, sessionId);
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return false;
    }
}
