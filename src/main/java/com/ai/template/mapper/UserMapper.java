package com.ai.template.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ai.template.model.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


public interface UserMapper extends BaseMapper<User> {

    
    @Update("UPDATE user SET quota = quota - 1 WHERE id = #{userId} AND quota > 0")
    int decrementQuota(@Param("userId") Long userId);

}