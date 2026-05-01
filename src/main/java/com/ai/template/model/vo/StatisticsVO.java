package com.ai.template.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long todayCount;

    private Long weekCount;

    private Long monthCount;

    private Long totalCount;

    private Double successRate;

    private Integer avgDurationMs;

    private Long activeUserCount;

    private Long totalUserCount;

    private Long vipUserCount;

    private Long quotaUsed;
}