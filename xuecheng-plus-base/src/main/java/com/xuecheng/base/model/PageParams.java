package com.xuecheng.base.model;

import lombok.Data;

/**
 * 分页参数
 */
@Data
public class PageParams {

    //当前页码
    private Long pageNo = 1L;

    //每页显示记录数
    private Long pageSize = 30L;

    public Long getPageNo() {
        return pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
