package rs.raf.novosti.repositories.category;

import rs.raf.novosti.entities.Category;

import java.util.List;

public interface CategoryRepository {
    Category findById(Long id);
    Category findByName(String name);
    List<Category> findAll(int page, int pageSize);
    List<Category> findAll();
    int count();
    int countNewsByCategoryId(Long categoryId);
    Category save(Category category);
    Category update(Category category);
    void delete(Long id);
}
