package com.ai.template.service;

import com.ai.template.model.entity.User;

public interface QuotaService {

    boolean hasQuota(User user);

    void consumeQuota(User user);

    void checkAndConsumeQuota(User user);
}
