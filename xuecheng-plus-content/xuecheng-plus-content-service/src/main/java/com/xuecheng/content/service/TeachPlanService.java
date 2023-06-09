package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaverTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * 课程计划管理相关接口
 */
public interface TeachPlanService {
    /**
     * 根据课程id查询课程计划
     * @param courseId  课程id
     * @return
     */
    public List<TeachPlanDto> findTeachPlanTree(Long courseId);

    /**
     * 新增/保存/修改课程计划
     * @param saverTeachPlanDto
     */
    public void saveTeachplan(SaverTeachPlanDto saverTeachPlanDto);

    /**
     * 根据课程id删除课程计划
     * @param id 课程计划id
     */
    public void deleteTeachplan(Long id);

    /**
     * 移动课程计划
     * @param move  移动类型
     * @param id    课程计划id
     */
    public void moveTeachplan(String move, Long id);


}
