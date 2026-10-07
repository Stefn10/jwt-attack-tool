package rs.raf.novosti.repositories.reaction;

import rs.raf.novosti.entities.ReactionType;
import rs.raf.novosti.repositories.PostgresAbstractRepository;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PostgresReactionRepository extends PostgresAbstractRepository implements ReactionRepository {

    // ==================== News reactions ====================

    @Override
    public void addOrUpdateNewsReaction(Long newsId, String sessionId, ReactionType type) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "INSERT INTO news_reactions (news_id, session_id, reaction_type) VALUES (?, ?, ?::reaction_type) " +
                "ON CONFLICT (news_id, session_id) DO UPDATE SET reaction_type = ?::reaction_type"
            );
            preparedStatement.setLong(1, newsId);
            preparedStatement.setString(2, sessionId);
            preparedStatement.setString(3, type.name());
            preparedStatement.setString(4, type.name());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }

    @Override
    public String getNewsReaction(Long newsId, String sessionId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT reaction_type FROM news_reactions WHERE news_id = ? AND session_id = ?"
            );
            preparedStatement.setLong(1, newsId);
            preparedStatement.setString(2, sessionId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("reaction_type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return null;
    }

    @Override
    public Map<String, Integer> countNewsReactions(Long newsId) {
        return countReactions("news_reactions", "news_id", newsId);
    }

    // ==================== Comment reactions ====================

    @Override
    public void addOrUpdateCommentReaction(Long commentId, String sessionId, ReactionType type) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "INSERT INTO comment_reactions (comment_id, session_id, reaction_type) VALUES (?, ?, ?::reaction_type) " +
                "ON CONFLICT (comment_id, session_id) DO UPDATE SET reaction_type = ?::reaction_type"
            );
            preparedStatement.setLong(1, commentId);
            preparedStatement.setString(2, sessionId);
            preparedStatement.setString(3, type.name());
            preparedStatement.setString(4, type.name());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }

    @Override
    public String getCommentReaction(Long commentId, String sessionId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT reaction_type FROM comment_reactions WHERE comment_id = ? AND session_id = ?"
            );
            preparedStatement.setLong(1, commentId);
            preparedStatement.setString(2, sessionId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("reaction_type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return null;
    }

    @Override
    public Map<String, Integer> countCommentReactions(Long commentId) {
        return countReactions("comment_reactions", "comment_id", commentId);
    }

    // ==================== Helper method ====================

    private Map<String, Integer> countReactions(String table, String column, Long id) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("likes", 0);
        counts.put("dislikes", 0);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT reaction_type, COUNT(*) as cnt FROM " + table +
                " WHERE " + column + " = ? GROUP BY reaction_type"
            );
            preparedStatement.setLong(1, id);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String type = resultSet.getString("reaction_type");
                int cnt = resultSet.getInt("cnt");
                if ("LIKE".equals(type)) counts.put("likes", cnt);
                if ("DISLIKE".equals(type)) counts.put("dislikes", cnt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return counts;
    }
}
