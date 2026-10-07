package rs.raf.novosti.repositories.news;

import rs.raf.novosti.entities.News;
import rs.raf.novosti.repositories.PostgresAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresNewsRepository extends PostgresAbstractRepository implements NewsRepository {

    private static final String SELECT_NEWS =
        "SELECT n.*, " +
        "u.name AS author_name, u.surname AS author_surname, " +
        "c.name AS category_name " +
        "FROM news n " +
        "JOIN users u ON n.author_id = u.user_id " +
        "JOIN categories c ON n.category_id = c.category_id ";

    @Override
    public News findById(Long id) {
        News news = null;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(SELECT_NEWS + "WHERE n.news_id = ?");
            preparedStatement.setLong(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                news = mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return news;
    }

    @Override
    public List<News> findAll(int page, int pageSize) {
        List<News> newsList = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                SELECT_NEWS + "ORDER BY n.published_time DESC LIMIT ? OFFSET ?"
            );
            preparedStatement.setInt(1, pageSize);
            preparedStatement.setInt(2, (page - 1) * pageSize);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                newsList.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return newsList;
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
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM news");
            if (resultSet.next()) total = resultSet.getInt(1);
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
    public News save(News news) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            String[] generatedColumns = {"news_id"};

            preparedStatement = connection.prepareStatement(
                "INSERT INTO news (title, text, author_id, category_id) VALUES (?, ?, ?, ?)",
                generatedColumns
            );
            preparedStatement.setString(1, news.getTitle());
            preparedStatement.setString(2, news.getText());
            preparedStatement.setLong(3, news.getAuthorId());
            preparedStatement.setLong(4, news.getCategoryId());
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                news.setId(resultSet.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return news;
    }

    @Override
    public News update(News news) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "UPDATE news SET title = ?, text = ?, category_id = ? WHERE news_id = ?"
            );
            preparedStatement.setString(1, news.getTitle());
            preparedStatement.setString(2, news.getText());
            preparedStatement.setLong(3, news.getCategoryId());
            preparedStatement.setLong(4, news.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return news;
    }

    @Override
    public void delete(Long id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement("DELETE FROM news WHERE news_id = ?");
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }

    @Override
    public void incrementVisits(Long newsId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "UPDATE news SET number_of_visits = number_of_visits + 1 WHERE news_id = ?"
            );
            preparedStatement.setLong(1, newsId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }

    @Override
    public List<News> search(String query, int page, int pageSize) {
        List<News> newsList = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                SELECT_NEWS +
                "WHERE n.title ILIKE ? OR n.text ILIKE ? " +
                "ORDER BY n.published_time DESC LIMIT ? OFFSET ?"
            );
            String like = "%" + query + "%";
            preparedStatement.setString(1, like);
            preparedStatement.setString(2, like);
            preparedStatement.setInt(3, pageSize);
            preparedStatement.setInt(4, (page - 1) * pageSize);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                newsList.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return newsList;
    }

    @Override
    public int countSearch(String query) {
        int total = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT COUNT(*) FROM news WHERE title ILIKE ? OR text ILIKE ?"
            );
            String like = "%" + query + "%";
            preparedStatement.setString(1, like);
            preparedStatement.setString(2, like);
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
    public List<News> findMostRead(int limit, int days) {
        List<News> newsList = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            // 'INTERVAL ? days' does not work as a placeholder, so we multiply the interval
            preparedStatement = connection.prepareStatement(
                SELECT_NEWS +
                "WHERE n.published_time >= NOW() - (INTERVAL '1 day' * ?) " +
                "ORDER BY n.number_of_visits DESC LIMIT ?"
            );
            preparedStatement.setInt(1, days);
            preparedStatement.setInt(2, limit);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                newsList.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return newsList;
    }

    @Override
    public List<News> findTopByReactions(int limit) {
        List<News> newsList = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                SELECT_NEWS +
                "LEFT JOIN news_reactions r ON r.news_id = n.news_id " +
                "GROUP BY n.news_id, u.name, u.surname, c.name " +
                "ORDER BY COUNT(r.reaction_id) DESC LIMIT ?"
            );
            preparedStatement.setInt(1, limit);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                newsList.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return newsList;
    }

    @Override
    public List<News> findRelatedByTags(Long newsId, int limit) {
        List<News> newsList = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT DISTINCT n.*, " +
                "u.name AS author_name, u.surname AS author_surname, c.name AS category_name " +
                "FROM news n " +
                "JOIN users u ON n.author_id = u.user_id " +
                "JOIN categories c ON n.category_id = c.category_id " +
                "JOIN news_tags nt ON nt.news_id = n.news_id " +
                "WHERE nt.tag_id IN (SELECT tag_id FROM news_tags WHERE news_id = ?) " +
                "AND n.news_id <> ? " +
                "ORDER BY n.published_time DESC LIMIT ?"
            );
            preparedStatement.setLong(1, newsId);
            preparedStatement.setLong(2, newsId);
            preparedStatement.setInt(3, limit);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                newsList.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return newsList;
    }

    @Override
    public List<News> findByTag(String tagValue, int page, int pageSize) {
        List<News> newsList = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT DISTINCT n.*, " +
                "u.name AS author_name, u.surname AS author_surname, c.name AS category_name " +
                "FROM news n " +
                "JOIN users u ON n.author_id = u.user_id " +
                "JOIN categories c ON n.category_id = c.category_id " +
                "JOIN news_tags nt ON nt.news_id = n.news_id " +
                "JOIN tags t ON t.tag_id = nt.tag_id " +
                "WHERE t.value = ? " +
                "ORDER BY n.published_time DESC LIMIT ? OFFSET ?"
            );
            preparedStatement.setString(1, tagValue);
            preparedStatement.setInt(2, pageSize);
            preparedStatement.setInt(3, (page - 1) * pageSize);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                newsList.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return newsList;
    }

    @Override
    public int countByTag(String tagValue) {
        int total = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT COUNT(DISTINCT n.news_id) FROM news n " +
                "JOIN news_tags nt ON nt.news_id = n.news_id " +
                "JOIN tags t ON t.tag_id = nt.tag_id " +
                "WHERE t.value = ?"
            );
            preparedStatement.setString(1, tagValue);
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
    public List<News> findByCategory(Long categoryId, int page, int pageSize) {
        List<News> newsList = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                SELECT_NEWS +
                "WHERE n.category_id = ? " +
                "ORDER BY n.published_time DESC LIMIT ? OFFSET ?"
            );
            preparedStatement.setLong(1, categoryId);
            preparedStatement.setInt(2, pageSize);
            preparedStatement.setInt(3, (page - 1) * pageSize);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                newsList.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return newsList;
    }

    @Override
    public int countByCategory(Long categoryId) {
        int total = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT COUNT(*) FROM news WHERE category_id = ?"
            );
            preparedStatement.setLong(1, categoryId);
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

    private News mapRow(ResultSet resultSet) throws SQLException {
        News news = new News();
        news.setId(resultSet.getLong("news_id"));
        news.setTitle(resultSet.getString("title"));
        news.setText(resultSet.getString("text"));
        news.setPublishedTime(resultSet.getTimestamp("published_time").toLocalDateTime());
        news.setNumberOfVisits(resultSet.getInt("number_of_visits"));
        news.setAuthorId(resultSet.getLong("author_id"));
        news.setCategoryId(resultSet.getLong("category_id"));
        news.setAuthorName(resultSet.getString("author_name") + " " + resultSet.getString("author_surname"));
        news.setCategoryName(resultSet.getString("category_name"));
        return news;
    }
}
