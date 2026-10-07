package rs.raf.novosti.repositories.comment;

import rs.raf.novosti.entities.Comment;
import rs.raf.novosti.repositories.PostgresAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresCommentRepository extends PostgresAbstractRepository implements CommentRepository {

    @Override
    public List<Comment> findByNewsId(Long newsId, int page, int pageSize) {
        List<Comment> comments = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT * FROM comments WHERE news_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?"
            );
            preparedStatement.setLong(1, newsId);
            preparedStatement.setInt(2, pageSize);
            preparedStatement.setInt(3, (page - 1) * pageSize);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                comments.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return comments;
    }

    @Override
    public int countByNewsId(Long newsId) {
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM comments WHERE news_id = ?");
            preparedStatement.setLong(1, newsId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) total = resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return total;
    }

    @Override
    public Comment save(Comment comment) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            String[] generatedColumns = {"comment_id"};

            preparedStatement = connection.prepareStatement(
                "INSERT INTO comments (news_id, author_name, text) VALUES (?, ?, ?)",
                generatedColumns
            );
            preparedStatement.setLong(1, comment.getNewsId());
            preparedStatement.setString(2, comment.getAuthorName());
            preparedStatement.setString(3, comment.getText());
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                comment.setId(resultSet.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return comment;
    }

    private Comment mapRow(ResultSet resultSet) throws SQLException {
        Comment comment = new Comment();
        comment.setId(resultSet.getLong("comment_id"));
        comment.setNewsId(resultSet.getLong("news_id"));
        comment.setAuthorName(resultSet.getString("author_name"));
        comment.setText(resultSet.getString("text"));
        comment.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return comment;
    }
}
