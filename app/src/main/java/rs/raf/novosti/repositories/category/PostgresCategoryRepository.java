package rs.raf.novosti.repositories.category;

import rs.raf.novosti.entities.Category;
import rs.raf.novosti.repositories.PostgresAbstractRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresCategoryRepository extends PostgresAbstractRepository implements CategoryRepository {

    @Override
    public Category findById(Long id) {
        Category category = null;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM categories WHERE category_id = ?");
            preparedStatement.setLong(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                category = mapRow(resultSet); // maps this table row into a Java object
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return category;
    }

    @Override
    public Category findByName(String name) {
        Category category = null;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM categories WHERE name = ?");
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                category = mapRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return category;
    }

    @Override
    public List<Category> findAll(int page, int pageSize) {
        List<Category> categories = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "SELECT * FROM categories ORDER BY category_id LIMIT ? OFFSET ?"
            );
            preparedStatement.setInt(1, pageSize);
            preparedStatement.setInt(2, (page - 1) * pageSize);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                categories.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return categories;
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM categories ORDER BY name");

            while (resultSet.next()) { // take the next row and read while rows exist
                categories.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(statement);
            this.closeConnection(connection);
        }

        return categories;
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
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM categories");

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
    public int countNewsByCategoryId(Long categoryId) {
        int total = 0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM news WHERE category_id = ?");
            preparedStatement.setLong(1, categoryId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                total = resultSet.getInt(1);
            }
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
    public Category save(Category category) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = this.newConnection();
            String[] generatedColumns = {"category_id"}; //

            preparedStatement = connection.prepareStatement(
                "INSERT INTO categories (name, description) VALUES (?, ?)", generatedColumns
            );
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());
            preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys(); // get the id of the newly created object from the database
            if (resultSet.next()) { // move to the next row; initially it points before the first returned row (i.e. nothing)
                category.setId(resultSet.getLong(1)); // read the first column from the ResultSet (only one was returned, but we must say which)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeResultSet(resultSet);
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return category;
    }

    @Override
    public Category update(Category category) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement(
                "UPDATE categories SET name = ?, description = ? WHERE category_id = ?"
            );
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());
            preparedStatement.setLong(3, category.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }

        return category;
    }

    @Override
    public void delete(Long id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.newConnection();
            preparedStatement = connection.prepareStatement("DELETE FROM categories WHERE category_id = ?");
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.closeStatement(preparedStatement);
            this.closeConnection(connection);
        }
    }

    private Category mapRow(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setId(resultSet.getLong("category_id"));
        category.setName(resultSet.getString("name"));
        category.setDescription(resultSet.getString("description"));
        return category;
    }
}
