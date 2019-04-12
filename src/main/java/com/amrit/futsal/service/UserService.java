package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAll();

    UserDTO getById(Long id);

    UserDTO saveUser(UserDTO userDTO);

    CustomResponse<UserDTO> updateUser(UserDTO userDTO);

    void deleteUser(Long id);

}
