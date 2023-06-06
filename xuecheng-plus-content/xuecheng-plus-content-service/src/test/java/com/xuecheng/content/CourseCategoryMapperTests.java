package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTests {

    @Resource
    CourseCategoryMapper categoryMapper;

    @Test
    public void testCourseCategoryMapper(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = categoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
