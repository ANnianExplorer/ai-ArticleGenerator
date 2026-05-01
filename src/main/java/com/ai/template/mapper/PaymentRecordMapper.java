package com.ai.template.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ai.template.model.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}