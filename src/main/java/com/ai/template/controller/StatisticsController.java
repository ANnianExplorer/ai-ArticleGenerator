package com.ai.template.controller;

import com.ai.template.annotation.AuthCheck;
import com.ai.template.common.BaseResponse;
import com.ai.template.common.ResultUtils;
import com.ai.template.constant.UserConstant;
import com.ai.template.model.vo.StatisticsVO;
import com.ai.template.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/statistics")
@Slf4j
@Tag(name = "StatisticsController", description = "Statistics API")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    
    @GetMapping("/overview")
    @Operation(summary = "Get system statistics")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<StatisticsVO> getStatistics() {
        StatisticsVO statistics = statisticsService.getStatistics();
        return ResultUtils.success(statistics);
    }
}