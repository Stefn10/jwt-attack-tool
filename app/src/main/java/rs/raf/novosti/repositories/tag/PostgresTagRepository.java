package rs.raf.novosti.repositories.tag;

import rs.raf.novosti.entities.Tag;
import rs.raf.novosti.repositories.PostgresAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresTagRepository extends PostgresAbstractRepository implements TagRepository {

    @Override
    public Tag findOrCreate(String value) {
        // first try to find an existing one
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();

            preparedStatement = connection.prepareStatement("SELECT * FROM tags WHERE value = ?");
            preparedStatement.setString(1, value.trim().toLowerCase());
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new Tag(resultSet.getLong("tag_id"), resultSet.getString("value"));
            }

            // not found, create a new one
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);

            String[] generatedColumns = {"tag_id"};
            preparedStatement = connection.prepareStatement(
                "INSERT INTO tags (value) VALUES (?)", generatedColumns
            );
            preparedStatement.setString(1, value.trim().toLowerCase());
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return new Tag(resultSet.getLong(1), value.trim().toLowerCase());
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
    public List<Tag> findByNewsId(Long newsId) {
        List<Tag> tags = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT t.tag_id, t.value FROM tags t " +
                "JOIN news_tags nt ON t.tag_id = nt.tag_id " +
                "WHERE nt.news_id = ?"
            );
            preparedStatement.setLong(1, newsId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                tags.add(new Tag(resultSet.getLong("tag_id"), resultSet.getString("value")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return tags;
    }

    @Override
    public void addTagToNews(Long newsId, Long tagId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "INSERT INTO news_tags (news_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING"
            );
            preparedStatement.setLong(1, newsId);
            preparedStatement.setLong(2, tagId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }

    @Override
    public void removeTagsFromNews(Long newsId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement("DELETE FROM news_tags WHERE news_id = ?");
            preparedStatement.setLong(1, newsId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }
}
