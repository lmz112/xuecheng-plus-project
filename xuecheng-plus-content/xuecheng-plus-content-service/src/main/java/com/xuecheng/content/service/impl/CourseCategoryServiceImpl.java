package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Resource
    CourseCategoryMapper categoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //调用mapper递归查询出分类信息
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = categoryMapper.selectTreeNodes(id);
        //先将list转成map，key就是结点的id，value就是CourseCategoryDto对象，filter就是将根节点过滤掉了（仔细看啊可能数据表就知道了）
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //定义一个List作为最终返回的list
        List<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();
        //从头遍历 courseCategoryList ，一边遍历一边找子节点放在父节点的 childrenTreeNodes
        courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).forEach(item ->{
            //存入父节点（如果它的父节点是根节点那么他自己就是一个父节点）
            if (item.getParentid().equals(id)){
                courseCategoryList.add(item);
            }
            //找到父节点
            CourseCategoryTreeDto courseCategoryParent = mapTemp.get(item.getParentid());
            if (courseCategoryParent != null) {
                if (courseCategoryParent.getChildrenTreeNodes() == null){
                    //如果该父节点的childrenTreeNodes属性为空，就new一个集合，因为要向该集合中存入子节点
                    courseCategoryParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());

                }
                //将父节点的子节点放入父节点的childrenTreeNodes中
                courseCategoryParent.getChildrenTreeNodes().add(item);
                //这里即使父节点已经被加进 courseCategoryList 但是还是会改变
            }


        });
        return courseCategoryList;
    }
}
