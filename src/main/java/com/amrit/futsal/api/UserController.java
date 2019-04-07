package com.amrit.futsal.api;

import com.amrit.futsal.model.UserDTO;
import com.amrit.futsal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("")
    public ResponseEntity<List<UserDTO>> getAll() {
        return new ResponseEntity<>(userService.getAll(), HttpStatus.OK);
    }


    //With path variable to get
    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDTO> getOne(
            @PathVariable("id") Long id
    ) {
        return new ResponseEntity<>(userService.findById(id), HttpStatus.OK);
    }

    //With pathvariable id to delete
//    @DeleteMapping("/{id}")
//    public void deleteUser(@PathVariable("id") Long id){
//        userService.deleteUser(id);
//    }



    //With Parameters id to delete
    @DeleteMapping("")
    public void deleteUser(@RequestParam("id") Long id){
        userService.deleteUser(id);
    }

    @PostMapping("")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO){
        UserDTO saveUser = userService.saveUser(userDTO);
        return  new ResponseEntity<>(saveUser,HttpStatus.OK);
    }

    @PutMapping("")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO){
        return userService.updateUser(userDTO);
    }

}
