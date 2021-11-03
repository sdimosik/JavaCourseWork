package com.example.qwewqeqweqwe.controller;

import com.example.qwewqeqweqwe.model.Schedule;
import com.example.qwewqeqweqwe.service.Service1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/s1")
public class Controller {

    private final Service1 service1;

    public Controller(Service1 service1) {
        this.service1 = service1;
    }

    @GetMapping("/getSchedule")
    public ResponseEntity<Schedule> getSchedule() {
        return new ResponseEntity<>(service1.generateSchedule(100), HttpStatus.OK);
    }
}
