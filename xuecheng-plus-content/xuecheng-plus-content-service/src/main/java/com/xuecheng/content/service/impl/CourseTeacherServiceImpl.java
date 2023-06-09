package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Resource
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> findTeacher(Long courseId) {
        // 拼装条件
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        // 查询
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        return courseTeachers;
    }

    @Transactional
    @Override
    public void saveTeacher(CourseTeacher courseTeacher) {
        // 判断该讲师是否存在
        CourseTeacher teacher = courseTeacherMapper.selectById(courseTeacher.getId());
        if (teacher == null){
            // 为空则保存
            courseTeacherMapper.insert(courseTeacher);
        }else{
            // 存在就更新
            courseTeacherMapper.updateById(courseTeacher);
        }

    }

    @Transactional
    @Override
    public void deleteById(Long courseId, Long id) {
        // 删除
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, id);
        courseTeacherMapper.delete(queryWrapper);
    }
}
