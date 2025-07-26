package br.com.devjf.salessync.service;

import br.com.devjf.salessync.dao.UserDAO;
import br.com.devjf.salessync.model.User;
import br.com.devjf.salessync.model.UserType;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {
    
    private final UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    public boolean createUser(String name, String login, String password, UserType type) {
        // Check if login already exists
        if (userDAO.findByLogin(login) != null) {
            return false;
        }
        
        User user = new User(name, login, password, type);
        return userDAO.save(user);
    }
    
    public User authenticateUser(String login, String password) {
        User user = userDAO.findByLogin(login);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    
    public boolean changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userDAO.findById(userId);
        if (user != null && BCrypt.checkpw(currentPassword, user.getPassword())) {
            user.changePassword(newPassword);
            return userDAO.update(user);
        }
        return false;
    }
    
    public boolean updateUser(Integer userId, String name, UserType type) {
        User user = userDAO.findById(userId);
        if (user != null) {
            user.setName(name);
            user.setType(type);
            return userDAO.update(user);
        }
        return false;
    }
    
    public User getUserById(Integer userId) {
        return userDAO.findById(userId);
    }
    
    public User getUserByLogin(String login) {
        return userDAO.findByLogin(login);
    }
    
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
}