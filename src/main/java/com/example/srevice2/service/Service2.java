package com.example.srevice2.service;

import com.example.srevice2.model.Schedule;
import com.example.srevice2.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;

@Service
public class Service2 {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String path = "src/main/resources/";
    private final Type scheduleType = new TypeToken<Schedule>() {
    }.getType();


    public String getSavedFileName(ResponseEntity<String> response) {

        Schedule schedule = gson.fromJson(response.getBody(), scheduleType);
        String fileName = path + "schedule" + Utils.generateInt(0, 100) + ".json";
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(gson.toJson(schedule));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileName;
    }

    public String getScheduleByName(String name) throws FileNotFoundException {
        File f = new File(path + name + ".json");
        if (!f.isFile() || !f.exists()) {
            throw new FileNotFoundException();
        }
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(f);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();

    }

    public void addShips(String name) throws IOException {
        try (FileReader fileReader = new FileReader(path + name)) {
            Schedule schedule = gson.fromJson(fileReader, scheduleType);
            Utils.addingSomeRecords(schedule);
            String fileName = path + name;
            try (FileWriter fileWriter = new FileWriter(fileName)) {
                fileWriter.write(gson.toJson(schedule));
            }
        }
    }

    public String savedReport(String report, String name) throws IOException {
        String pathFile = path + name;
        FileWriter fileWriter = new FileWriter(pathFile);
        fileWriter.write(report);
        fileWriter.close();
        return pathFile;
    }

    public String checkTimetables() {
        /*
         String[] filenames;
        File file = new File(path);
        filenames = file.list();
        StringBuilder stringBuilder = new StringBuilder();

        for (String currentFile : filenames) {
            if (currentFile.startsWith("schedule")) {
                stringBuilder.append(currentFile).append('\n');
            }
        }
         */
        List<String> fileNames;
        File file = new File(path);
        if (file.list() == null) {
            fileNames = List.of();
        } else {
            fileNames = List.of(file.list());
        }
        StringBuilder stringBuilder = new StringBuilder();

        for (String currentFile : fileNames) {
            if (currentFile.startsWith("schedule")) {
                stringBuilder.append(currentFile).append('\n');
            }
        }
        return stringBuilder.toString();
    }

    public String checkReports() {
        String[] filenames;
        File file = new File(path);
        filenames = file.list();
        StringBuilder stringBuilder = new StringBuilder();

        for (String currentFile : filenames) {
            if (currentFile.startsWith("report")) {
                stringBuilder.append(currentFile).append('\n');
            }
        }
        return stringBuilder.toString();
    }

    public String checkReport(String name) throws FileNotFoundException {
        File f = new File(path + name + ".json");
        if (!f.isFile() || !f.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(f);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }
}

