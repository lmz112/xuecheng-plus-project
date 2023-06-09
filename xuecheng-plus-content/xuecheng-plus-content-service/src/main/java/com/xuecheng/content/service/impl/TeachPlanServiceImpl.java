package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaverTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Resource
    TeachplanMapper teachplanMapper;

    @Resource
    TeachplanMediaMapper teachplanMediaMapper;


    @Override
    public List<TeachPlanDto> findTeachPlanTree(Long courseId) {
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachPlanDtos;
    }

    @Transactional
    @Override
    public void saveTeachplan(SaverTeachPlanDto saverTeachPlanDto) {
        // 通过课程计划id判断是新增还是修改
        Long techplanId = saverTeachPlanDto.getId();
        if (techplanId == null){
            // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saverTeachPlanDto, teachplan);
            // 确定排序字段，找到它的同级节点个数，排序字段就是个数+1
            Integer count = getTeachplanCount(saverTeachPlanDto);
            teachplan.setOrderby(count);
            
            teachplanMapper.insert(teachplan);
        }else {
            // 修改
            Teachplan teachplan = teachplanMapper.selectById(techplanId);
            BeanUtils.copyProperties(saverTeachPlanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    private Integer getTeachplanCount(SaverTeachPlanDto saverTeachPlanDto) {
        Long parentid = saverTeachPlanDto.getParentid();
        Long courseId = saverTeachPlanDto.getCourseId();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentid);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long id) {
        // 查询相课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);

        // 判断该结点是否为父结点
        if (teachplan.getParentid() == 0){
            // 是父节点
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, id);
            // 查询该父节点的子节点有哪些
            List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
            // 如果有子节点则不能删除
            if (!teachplans.isEmpty()){
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }else{
                // 没有则删除
                teachplanMapper.deleteById(id);
            }
        }else{
            // 删除子节点
            teachplanMapper.deleteById(id);
            // 删除teachplan_media表关联的信息
            Long teachplanId = teachplan.getId();
            // 拼接条件
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId, teachplanId);
            teachplanMediaMapper.delete(queryWrapper);
        }

    }

    @Override
    public void moveTeachplan(String move, Long id) {
        // 查询课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);
        // 判断该节点是否为第一个
        Integer orderby = teachplan.getOrderby();
        move(teachplan, move);
    }

    private void move(Teachplan teachplan, String code){
        // 获取排序字段
        Integer orderby = teachplan.getOrderby();

        // 判断是上移还是下移
        if (code.equals("moveup")){
            if (orderby > 0){
                orderby --;
            }else{
                return;
            }
        }else{
            orderby ++;
        }

        // 查询出该节点上面的节点
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getOrderby, orderby)
                .eq(Teachplan::getParentid, teachplan.getParentid());
        Teachplan teachplanUP = teachplanMapper.selectOne(queryWrapper);
        // 将下一节点的排序值设为当前节点的  之所以重新获取是因为orderby已经更改
        teachplanUP.setOrderby(teachplan.getOrderby());
        teachplan.setOrderby(orderby);
        // 修改数据库库
        teachplanMapper.updateById(teachplanUP);
        teachplanMapper.updateById(teachplan);
    }




}
