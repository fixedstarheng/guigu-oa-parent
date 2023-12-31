package com.atguigu.process.service;

import com.atguigu.model.process.ProcessTemplate;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author lhy
 * @since 2023-06-02
 */
public interface OaProcessTemplateService extends IService<ProcessTemplate> {

    IPage<ProcessTemplate> selectPageProcessTempate(Page<ProcessTemplate> pageParam);
    /*发布 = 部署流程定义*/
    void publish(Long id);
}
