package com.amrit.futsal.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @RequestMapping(path = "" )
    public String mainEntry(){
        return "main Entry";

    }
}
