package com.example.demo.service;

import com.example.demo.dao.TestDao;
import com.example.demo.model.TestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestDao testDao;


    public void insertCheck(TestDto testDto) {
        
    }
}
