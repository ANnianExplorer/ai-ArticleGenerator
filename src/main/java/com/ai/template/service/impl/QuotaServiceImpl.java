package com.ai.template.service.impl;

import com.ai.template.exception.BusinessException;
import com.ai.template.exception.ErrorCode;
import com.ai.template.mapper.UserMapper;
import com.ai.template.model.entity.User;
import com.ai.template.service.QuotaService;
import com.ai.template.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ai.template.constant.UserConstant.ADMIN_ROLE;
import static com.ai.template.constant.UserConstant.VIP_ROLE;

@Service
@Slf4j
public class QuotaServiceImpl implements QuotaService {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean hasQuota(User user) {

        if (isAdmin(user) || isVip(user)) {
            return true;
        }

        User freshUser = userService.getById(user.getId());
        if (freshUser == null) {
            return false;
        }
        Integer quota = freshUser.getQuota();
        return quota != null && quota > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeQuota(User user) {

            return;
        }



        if (affectedRows > 0) {
            log.info("用户配额已消�? userId={}", user.getId());
        } else {
            log.warn("用户配额扣减失败（可能配额不足或并发冲突�? userId={}", user.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndConsumeQuota(User user) {

            return;
        }

        int affectedRows = userMapper.decrementQuota(user.getId());

        if (affectedRows == 0) {

            throw new BusinessException(ErrorCode.OPERATION_ERROR, "配额不足，无法创建文�?);
        }

        log.info("用户配额检查并消耗成�? userId={}", user.getId());
    }

    private boolean isAdmin(User user) {
        return ADMIN_ROLE.equals(user.getUserRole());
    }

    private boolean isVip(User user) {
        return VIP_ROLE.equals(user.getUserRole());
    }
}
