package rs.raf.novosti.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.apache.commons.codec.digest.DigestUtils;
import rs.raf.novosti.entities.User;
import rs.raf.novosti.entities.UserType;
import rs.raf.novosti.repositories.user.UserRepository;
import rs.raf.novosti.requests.CreateUserRequest;

import javax.inject.Inject;
import java.util.List;

public class UserService {

    @Inject
    private UserRepository userRepository;

    public String login(String email, String password) {
        String hashedPassword = DigestUtils.sha256Hex(password);

        User user = this.userRepository.findByEmail(email);
        if (user == null || !user.getHashedPassword().equals(hashedPassword) || !user.isStatus()) {
            return null;
        }

        Algorithm algorithm = Algorithm.HMAC256("secret");

        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("role", user.getUserType().name())
                .withClaim("userId", user.getId())
                .sign(algorithm);
    }

    public boolean isAuthorized(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);

            String email = jwt.getSubject();
            User user = this.userRepository.findByEmail(email);

            return user != null && user.isStatus();
        } catch (Exception e) {
            return false;
        }
    }

    public List<User> allUsers(int page, int pageSize) {
        return this.userRepository.findAll(page, pageSize);
    }

    public int userCount() {
        return this.userRepository.count();
    }

    public User findUser(Long id) {
        return this.userRepository.findById(id);
    }

    public User addUser(CreateUserRequest request) {
        User existing = this.userRepository.findByEmail(request.getEmail());
        if (existing != null) {
            return null;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setUserType(request.getUserType());
        user.setStatus(true);
        user.setHashedPassword(DigestUtils.sha256Hex(request.getPassword()));

        return this.userRepository.save(user);
    }

    public User updateUser(Long id, User userData) {
        User user = this.userRepository.findById(id);
        if (user == null) {
            return null;
        }

        User emailUser = this.userRepository.findByEmail(userData.getEmail());
        if (emailUser != null && !emailUser.getId().equals(id)) {
            return null;
        }

        user.setEmail(userData.getEmail());
        user.setName(userData.getName());
        user.setSurname(userData.getSurname());
        user.setUserType(userData.getUserType());

        return this.userRepository.update(user);
    }

    public void toggleStatus(Long id) {
        User user = this.userRepository.findById(id);
        if (user != null && user.getUserType() == UserType.CREATOR) {
            this.userRepository.updateStatus(id, !user.isStatus());
        }
    }
}
