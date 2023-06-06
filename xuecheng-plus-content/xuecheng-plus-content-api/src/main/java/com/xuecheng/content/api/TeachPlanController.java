package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaverTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程计划管理相关接口
 */
@Api(value = "课程计划管理相关接口", tags = "课程计划管理相关接口")
@RestController
public class TeachPlanController {

    @Resource
    TeachPlanService teachPlanService;

    @ApiOperation("课程计划查询接口")
    @GetMapping("teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        List<TeachPlanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        return teachPlanTree;
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaverTeachPlanDto saverTeachPlanDto){
        teachPlanService.saveTeachplan(saverTeachPlanDto);
    }

    @ApiOperation("课程计划删除接口")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan(@PathVariable Long id){
        teachPlanService.deleteTeachplan(id);
    }

}
