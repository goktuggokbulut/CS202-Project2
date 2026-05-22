package service;

import model.User;
import repository.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }
        return userRepository.login(username, password);
    }

    public boolean register(User user, String type, java.util.List<String> addresses, java.util.List<String> phones) {
        // Basic validation
        if (user.getUsername() == null || user.getUsername().length() < 3) return false;
        if (user.getPassword() == null || user.getPassword().length() < 6) return false;
        
        return userRepository.register(user, type, addresses, phones);
    }
}
