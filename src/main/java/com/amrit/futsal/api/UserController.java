package com.amrit.futsal.api;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.UserDTO;
import com.amrit.futsal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public CustomResponse getAll() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<UserDTO> userDTOS = userService.getAll();
            if (userDTOS.size() == 0) {
                customResponse.setStatus(404);
                customResponse.setMessage("Success");
                return customResponse;
            }
            map.put("users", userDTOS);
            customResponse.setStatus(200);
            customResponse.setBody(map);
            return customResponse;

        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }


    //With path variable to get
    @GetMapping(path = "/{id}")
    public CustomResponse<UserDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            UserDTO userDTO = userService.findById(id);
            if (userDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("User with id :" + id + " not found");
                return customResponse;
            }
            map.put("user", userDTO);
            customResponse.setStatus(200);
            customResponse.setMessage("Successfully retrieved");
            customResponse.setBody(map);
            return customResponse;
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    //With pathvariable id to delete
//    @DeleteMapping("/{id}")
//    public void deleteUser(@PathVariable("id") Long id){
//        userService.deleteUser(id);
//    }


    //With Parameters id to delete
    @DeleteMapping("")
    public void deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
    }

    @PostMapping("")
    public CustomResponse<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            UserDTO saveUser = userService.saveUser(userDTO);
            if(saveUser == null){
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("User saved");
            customResponse.setStatus(200);
            customResponse.setBody(map);
            return customResponse;
        }catch (Exception e){
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @PutMapping("")
    public CustomResponse<UserDTO> updateUser(@RequestBody UserDTO userDTO) {

        return userService.updateUser(userDTO);
    }

}
