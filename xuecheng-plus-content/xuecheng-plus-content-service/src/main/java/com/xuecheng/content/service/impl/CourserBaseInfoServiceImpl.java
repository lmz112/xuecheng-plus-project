package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CourserBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根据名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        //根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
        //根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()), CourseBase::getStatus, courseParamsDto.getPublishStatus());

        //创建page分页参数对象, 参数：当前页码，每页记录数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //获取数据
        List<CourseBase> records = pageResult.getRecords();
        //获取总记录数
        long total = pageResult.getTotal();

        //结果
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        // 1.1.参数合法性校验
        /*if (StringUtils.isBlank(addCourseDto.getName())) {
             XueChengPlusException.cast("课程名称为空");
        }
        if (StringUtils.isBlank(addCourseDto.getMt())) {
             XueChengPlusException.cast("课程分类为空");
        }
        if (StringUtils.isBlank(addCourseDto.getSt())) {
             XueChengPlusException.cast("课程分类为空");
        }
        if (StringUtils.isBlank(addCourseDto.getGrade())) {
             XueChengPlusException.cast("课程等级为空");
        }
        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
             XueChengPlusException.cast("教育模式为空");
        }
        if (StringUtils.isBlank(addCourseDto.getUsers())) {
             XueChengPlusException.cast("适应人群为空");
        }
        if (StringUtils.isBlank(addCourseDto.getCharge())) {
             XueChengPlusException.cast("收费规则为空");
        }*/

        // 2.向课程信息基本表course_base写入数据
        // 2.1.新建CourseBase数据对象
        CourseBase courseBaseNew = new CourseBase();
        // 2.2.copy对象数据
        BeanUtils.copyProperties(addCourseDto, courseBaseNew);
        // 2.3.设置剩余属性
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        // 审核状态和发布状态都默认为未审核和未发布
        courseBaseNew.setAuditStatus("202002");
        courseBaseNew.setStatus("203001");
        // 2.4.向数据库插入数据
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0){
             XueChengPlusException.cast("添加课程失败");
        }

        // 3.向课程营销表course_market写入数据
        // 3.1.新建数据对象
        CourseMarket courseMarketNew = new CourseMarket();
        // 3.2.copy对象数据
        BeanUtils.copyProperties(addCourseDto, courseMarketNew);
        // 3.3.设置课程id
        // 因为基本表和营销表是通过id一对一连接的，所以他俩的id是相同的
        Long courseId = courseBaseNew.getId();
        courseMarketNew.setId(courseId);
        // 3.4.保存营销信息
        saveCourseMarket(courseMarketNew);

        // 4.从数据库查询课程的详细信息
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);

        return courseBaseInfo;
    }

    // 查询课程信息
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        // 从课程基本信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            return null;
        }

        // 从课程营销表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // 拼装在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        // 通过 courseCategoryMapper 查询分类信息，将分类名称放在courseBaseInfoDto对象
        CourseCategory courseCategorySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategorySt.getName());
        CourseCategory courseCategoryMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryMt.getName());

        return courseBaseInfoDto;
    }

    // 保存营销信息，逻辑：存在则更新，不存在则添加
    private int saveCourseMarket(CourseMarket courseMarketNew){
        // 参数合法性校验
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isEmpty(charge)){
             XueChengPlusException.cast("收费规则为空");
        }
        // 如果课程收费，价格没有填写也需要抛出异常
        if (charge.equals("201001")){
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue() <= 0){
                 XueChengPlusException.cast("课程的价格不能为空且必须大于0");
            }
        }

        // 从数据库查询营销信息，存在则更新，不存在则添加
        Long id = courseMarketNew.getId();
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket == null){
            // 插入数据库
            int insert = courseMarketMapper.insert(courseMarketNew);
            return insert;
        }else{
            // copy数据
            BeanUtils.copyProperties(courseMarketNew, courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            // 更新
            int i = courseMarketMapper.updateById(courseMarket);
            return i;
        }

    }
}
