package rs.raf.novosti.repositories.user;

import rs.raf.novosti.entities.User;

import java.util.List;

public interface UserRepository {
    User findById(Long id);
    User findByEmail(String email);
    List<User> findAll(int page, int pageSize);
    int count();
    User save(User user);
    User update(User user);
    void updateStatus(Long id, boolean status);
}
