package com.atguigu.process.mapper;

import com.atguigu.model.process.ProcessRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 审批记录 Mapper 接口
 * </p>
 *
 * @author lhy
 * @since 2023-06-06
 */
//@Mapper 我在oa process record serviceimpl 里使用了baseMapper
public interface OaProcessRecordMapper extends BaseMapper<ProcessRecord> {

}
