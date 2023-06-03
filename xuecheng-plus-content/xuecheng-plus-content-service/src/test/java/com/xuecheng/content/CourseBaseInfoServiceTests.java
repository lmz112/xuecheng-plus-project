package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class CourseBaseInfoServiceTests {
    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testCourseBaseInfoService() {

        //查询条件
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        //课程名称查询条件
        courseParamsDto.setCourseName("java");
        //发布状态查询条件
        courseParamsDto.setPublishStatus("203001");

        //分页参数对象
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(2L);

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, courseParamsDto);
        System.out.println(courseBasePageResult);
    }
}
