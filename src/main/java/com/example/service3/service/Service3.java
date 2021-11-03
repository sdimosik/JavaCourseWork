package com.example.service3.service;

import com.example.service3.model.Report;
import com.example.service3.model.Schedule;
import com.example.service3.utils.Simulate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

@Service
public class Service3 {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type scheduleType = new TypeToken<Schedule>() {
    }.getType();

    public Report simulate(ResponseEntity<String> responseSchedule) {
        Schedule schedule = gson.fromJson(responseSchedule.getBody(), scheduleType);
        Simulate simulate = new Simulate();
        return simulate.run(schedule);
    }

    public String reportToJson(Report report){
        return gson.toJson(report);
    }
}
