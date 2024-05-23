package com.example.demo.dao;

import com.example.demo.model.TestDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestDao {

    public void insertData(TestDto testDto);

}
