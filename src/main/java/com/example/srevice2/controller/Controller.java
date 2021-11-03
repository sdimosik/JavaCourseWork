package com.example.srevice2.controller;

import com.example.srevice2.service.Service2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/s2")
public class Controller {

    private final Service2 service2;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String s1URL = "http://localhost:10000/s1/getSchedule";

    public Controller(Service2 service2) {
        this.service2 = service2;
    }

    @GetMapping("/getSchedule")
    public ResponseEntity<String> getSchedule() {

        ResponseEntity<String> response = restTemplate.getForEntity(s1URL, String.class);
        return new ResponseEntity<>(service2.getSavedFileName(response), HttpStatus.OK);
    }

    @GetMapping("/getScheduleByName/{name}")
    public ResponseEntity<String> getScheduleByName(@PathVariable String name) throws ResponseStatusException {
        try {
            return new ResponseEntity<>(service2.getScheduleByName(name), HttpStatus.OK);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/addShips/{name}")
    public ResponseEntity<Boolean> addShips(@PathVariable String name) {
        try {
            service2.addShips(name);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PostMapping("/report/{name}")
    public ResponseEntity<String> savedReport(@RequestBody String report, @PathVariable String name) {
        try {
            String pathFile = service2.savedReport(report, name);
            return new ResponseEntity<>(pathFile, HttpStatus.OK);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/listSchedule")
    public ResponseEntity<String> checkTimetables() {
        return new ResponseEntity<>(service2.checkTimetables(), HttpStatus.OK);
    }

    @GetMapping("/listReports")
    public ResponseEntity<String> checkReports() {
        return new ResponseEntity<>(service2.checkReports(), HttpStatus.OK);
    }

    @GetMapping("/checkReport/{name}")
    public ResponseEntity<String> checkReport(@PathVariable String name) {
        try {
            return new ResponseEntity<>(service2.checkReport(name), HttpStatus.OK);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
