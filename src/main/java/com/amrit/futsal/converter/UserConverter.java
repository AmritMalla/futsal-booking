package com.amrit.futsal.converter;

import com.amrit.futsal.entity.User;
import com.amrit.futsal.model.UserDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class UserConverter {

    public UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setEmailVerificationCode(user.getEmailVerificationCode());
        userDTO.setEmailValidated(user.getEmailValidated());
        userDTO.setMobileNumber(user.getMobileNumber());
        userDTO.setMobileVerificationCode(user.getMobileVerificationCode());
        userDTO.setPassword(user.getPassword());
        userDTO.setPasswordResetCode(user.getPasswordResetCode());
        return userDTO;
    }

    public List<UserDTO> convertToDtoList(List<User> userList) {
        List<UserDTO> userDTOS = new ArrayList<>();
        Iterator<User> userIterator = userList.iterator();
        while (userIterator.hasNext()) {
            userDTOS.add(convertToDto(userIterator.next()));
        }
        return userDTOS;
    }

    public User convertToEntity(UserDTO userDTO) {
        User user = new User();

        if(userDTO.getId() != null){
            user.setId(userDTO.getId());
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setEmailVerificationCode(userDTO.getEmailVerificationCode());
        user.setMobileNumber(userDTO.getMobileNumber());
        user.setEmailValidated(userDTO.getEmailValidated());
        user.setMobileVerificationCode(userDTO.getMobileVerificationCode());
        user.setPassword(userDTO.getPassword());
        user.setPasswordResetCode(userDTO.getPasswordResetCode());
        return user;

    }

    List<User> convertToEntityList(List<UserDTO> userDTOS) {
        List<User> userList = new ArrayList<>();
        Iterator<UserDTO> userDTOIterator = userDTOS.iterator();
        while (userDTOIterator.hasNext()) {
            userList.add(convertToEntity(userDTOIterator.next()));
        }
        return userList;
    }
}
