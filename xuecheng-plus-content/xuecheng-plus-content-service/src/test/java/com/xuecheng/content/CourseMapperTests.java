package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.po.CourseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 课程查询测试类
 */
@SpringBootTest
public class CourseMapperTests {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper(){
       CourseBase courseBase = courseBaseMapper.selectById(18);

    }
}
