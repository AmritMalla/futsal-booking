package com.amrit.futsal.service;

import com.amrit.futsal.model.UserDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    List<UserDTO> getAll();

    UserDTO findById(Long id);

    UserDTO saveUser(UserDTO userDTO);

    ResponseEntity<UserDTO> updateUser(UserDTO userDTO);

    void deleteUser(Long id);

}
