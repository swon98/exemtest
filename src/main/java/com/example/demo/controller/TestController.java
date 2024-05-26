package com.example.demo.controller;

import com.example.demo.model.TestDto;
import com.example.demo.service.ApiService;
import com.example.demo.service.TestService;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final ApiService apiService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

    @GetMapping("/")
    public void fileLead(){

        JSONParser parser = new JSONParser();

        //json 읽어오기
        try {
            ClassPathResource resource = new ClassPathResource("json/dust.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), "UTF-8");
            JSONArray dataArray = (JSONArray) parser.parse(reader);

            List<JSONObject> stationData = new ArrayList<>();
            for (Object obj : dataArray) {
                JSONObject data = (JSONObject) obj;
                stationData.add(data);
            }

            // 시간 순서대로 데이터 처리
            processStationData(stationData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processStationData(List<JSONObject> stationData) {
        int checkTime = 0;
        LocalDateTime startDateTime = null;
        String lastStationName = null;

        for (int i = 0; i < stationData.size(); i++) {
            JSONObject data = stationData.get(i);
            String date = (String) data.get("날짜");
            String station = (String) data.get("측정소명");
            String fineDustStr = (String) data.get("PM10");
            String ultrafineDustStr = (String) data.get("PM2.5");

            int fineDust = fineDustStr != null ? Integer.parseInt(fineDustStr) : 0;
            int ultrafineDust = ultrafineDustStr != null ? Integer.parseInt(ultrafineDustStr) : 0;

            // 측정값이 없으면 점검 내역으로 저장
            if (fineDustStr == null && ultrafineDustStr == null) {
                TestDto testDto = new TestDto();
                testDto.setGrade("0");
                testDto.setLevel("점검");
                testDto.setStation(station);
                testDto.setDate(date);
                testDto.setFineDust("0");
                testDto.setUltrafineDust("0");
                testService.insertData(testDto);
                continue;
            }

            //측정소별 초기화
            if (!station.equals(lastStationName)) {
                checkTime = 0;
                startDateTime = null;
                lastStationName = station;
            }

            // 미세먼지 또는 초미세먼지가 기준치를 넘으면 연속 시간 증가
            if (fineDust >= 150 || ultrafineDust >= 75) {
                if (checkTime == 0) {
                    startDateTime = LocalDateTime.parse(date, formatter);
                }
                checkTime++;

                if (checkTime >= 2) {
                    int grade = gradeCheck(fineDust, ultrafineDust);
                    String level = levelCheck(fineDust, ultrafineDust);
                    if (grade > 0) {
                        TestDto testDto = new TestDto();
                        testDto.setGrade(String.valueOf(grade));
                        testDto.setLevel(level);
                        testDto.setStation(station);
                        testDto.setDate(date);
                        testDto.setFineDust(fineDustStr);
                        testDto.setUltrafineDust(ultrafineDustStr);
                        testService.insertData(testDto);

                        HashMap<String, String> requestMap = new HashMap<>();

                        requestMap.put("grade", String.valueOf(grade));
                        requestMap.put("level", level);
                        requestMap.put("station", station);
                        requestMap.put("date", date);

                        String apiresult = apiService.call("api.climate", requestMap);
                    }
                }
            } else {
                checkTime = 0;
                startDateTime = null;
            }

            if (i == stationData.size() - 1 && checkTime >= 2) {
                int grade = gradeCheck(fineDust, ultrafineDust);
                String level = levelCheck(fineDust, ultrafineDust);
                if (grade > 0) {
                    TestDto testDto = new TestDto();
                    testDto.setGrade(String.valueOf(grade));
                    testDto.setLevel(level);
                    testDto.setStation(station);
                    testDto.setDate(date);
                    testDto.setFineDust(fineDustStr);
                    testDto.setUltrafineDust(ultrafineDustStr);
                    testService.insertData(testDto);
                }
            }
        }
    }

    private int gradeCheck(int fineDust, int ultrafineDust) {
        if (ultrafineDust >= 150) {
            return 1;
        } else if (fineDust >= 300) {
            return 2;
        } else if (ultrafineDust >= 75) {
            return 3;
        } else if (fineDust >= 150) {
            return 4;
        } else {
            return 0;
        }
    }
    private String levelCheck(int fineDust, int ultrafineDust) {
        if (ultrafineDust >= 150) {
            return "초미세먼지 경보";
        } else if (fineDust >= 300) {
            return "미세먼지 경보";
        } else if (ultrafineDust >= 75) {
            return "초미세먼지 주의보";
        } else if (fineDust >= 150) {
            return "미세먼지 주의보";
        } else {
            return "점검";
        }
    }
}