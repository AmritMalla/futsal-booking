package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.UserConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.model.UserDTO;
import com.amrit.futsal.repository.UserRepository;
import com.amrit.futsal.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserConverter userConverter;

    @Override
    public List<UserDTO> getAll() {
        return userConverter.convertToDtoList(userRepository.findAll());
    }

    @Override
    public UserDTO getById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return userConverter.convertToDto(optionalUser.get());
    }

    @Override
    public UserDTO saveUser(UserDTO userDTO) {
        User user = userConverter.convertToEntity(userDTO);
        User save = userRepository.save(user);
        return userConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<UserDTO> updateUser(UserDTO userDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<User> optionalUser = userRepository.findById(userDTO.getId());

            if (!optionalUser.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("User with given id doesn't exist");
                return customResponse;
            }
            User user = userConverter.convertToEntity(userDTO);
            User save = userRepository.save(user);
            map.put("user", userConverter.convertToDto(save));
            customResponse.setStatus(200);
            customResponse.setMessage("Successfully updated");
            customResponse.setBody(map);
            return customResponse;

        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
