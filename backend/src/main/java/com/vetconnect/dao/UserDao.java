package com.vetconnect.dao;

import com.vetconnect.model.RegisterUserDto;
import com.vetconnect.model.User;

import java.util.List;

public interface UserDao {

    List<User> getUsers();

    User getUserById(int id);

    User getUserByUsername(String username);

    User createUser(RegisterUserDto user);
}
