package com.xuecheng.content.api;

import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "课程讲师相关接口", tags = "课程讲师相关接口")
@RestController
public class CourseTeacherController {

    @Resource
    CourseTeacherService courseTeacherService;

    @ApiOperation("课程讲师查询接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> findTeacher(@PathVariable Long courseId){

        return courseTeacherService.findTeacher(courseId);
    }

    @ApiOperation("保存/修改讲师接口")
    @PostMapping("/courseTeacher")
    public void saveTeacher(@RequestBody CourseTeacher courseTeacher){
        // todo:只允许机构自己的课程中条件、修改讲师
        courseTeacherService.saveTeacher(courseTeacher);
    }


    @ApiOperation("删除讲师接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void deleteTeacher(@PathVariable Long courseId, @PathVariable Long id){
        courseTeacherService.deleteById(courseId, id);
    }
}
