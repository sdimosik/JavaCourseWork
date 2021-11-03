package com.example.service3.controller;

import com.example.service3.model.Report;
import com.example.service3.service.Service3;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/s3")
public class Controller {

    private final Service3 service3;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String s2URL = "http://localhost:20000/s2/";

    public Controller(Service3 service3) {
        this.service3 = service3;
    }

    @GetMapping("/calc/{name}")
    public ResponseEntity<Boolean> simulate(@PathVariable String name) {

        ResponseEntity<String> responseSchedule = restTemplate.getForEntity(s2URL + "getScheduleByName/" + name, String.class);
        Report report = service3.simulate(responseSchedule);

        String reportName = "report-" + name + ".json";
        restTemplate.postForEntity(s2URL + "report/" + reportName, service3.reportToJson(report), String.class);

        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
