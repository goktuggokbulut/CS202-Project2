package service;

import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }
        return userRepository.login(username, password);
    }

    public boolean register(User user, String type, java.util.List<String> addresses, java.util.List<String> phones) {
        if (user.getUsername() == null || user.getUsername().length() < 3) return false;
        if (user.getPassword() == null || user.getPassword().length() < 6) return false;
        return userRepository.register(user, type, addresses, phones);
    }
}
