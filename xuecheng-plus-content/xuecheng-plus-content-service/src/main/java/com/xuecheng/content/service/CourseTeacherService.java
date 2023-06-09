package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * 课程讲师相关接口
 */
public interface CourseTeacherService {
    /**
     * 根据课程id寻找讲师
     * @param courseId 课程id
     * @return    该课程的讲师
     */
    public List<CourseTeacher> findTeacher(Long courseId);

    /**
     * 保存讲师
     * @return
     */
    public void saveTeacher(CourseTeacher courseTeacher);


    /**
     * 根据id删除讲师
     * @param courseId  课程id
     * @param id  讲师id
     */
    public void deleteById(Long courseId, Long id);
}
