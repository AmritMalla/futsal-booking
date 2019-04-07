package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.UserConverter;
import com.amrit.futsal.entity.User;
import com.amrit.futsal.model.UserDTO;
import com.amrit.futsal.repository.UserRepository;
import com.amrit.futsal.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public UserDTO findById(Long id) {
//        User user = userRepository.findById(id).orElse(new User(0L));
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
    public ResponseEntity<UserDTO> updateUser(UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(userDTO.getId());
        if(!optionalUser.isPresent()){
            return ResponseEntity.notFound().build();
        }
        User user = userConverter.convertToEntity(userDTO);
        User save = userRepository.save(user);
        return new ResponseEntity<>(userConverter.convertToDto(save), HttpStatus.NO_CONTENT);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
