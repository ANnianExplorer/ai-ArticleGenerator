package com.ai.template.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.ai.template.constant.UserConstant;
import com.ai.template.mapper.ArticleMapper;
import com.ai.template.mapper.UserMapper;
import com.ai.template.model.entity.Article;
import com.ai.template.model.entity.User;
import com.ai.template.model.enums.ArticleStatusEnum;
import com.ai.template.model.vo.StatisticsVO;
import com.ai.template.service.AgentLogService;
import com.ai.template.service.StatisticsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private static final String STATISTICS_CACHE_KEY = "statistics:overview";
    private static final long CACHE_EXPIRE_HOURS = 1L;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AgentLogService agentLogService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public StatisticsVO getStatistics() {

        StatisticsVO cachedStats = (StatisticsVO) redisTemplate.opsForValue().get(STATISTICS_CACHE_KEY);
        if (cachedStats != null) {
            log.info("从缓存获取统计数�?);
            return cachedStats;
        }


        Long todayCount = countArticlesByDateRange(getTodayStart(), LocalDateTime.now());

        Long weekCount = countArticlesByDateRange(getWeekStart(), LocalDateTime.now());

        Long monthCount = countArticlesByDateRange(getMonthStart(), LocalDateTime.now());



        Integer avgDurationMs = calculateAvgDuration();

        Long activeUserCount = countActiveUsers(getWeekStart());

        Long totalUserCount = countTotalUsers();



        StatisticsVO statistics = StatisticsVO.builder()
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .totalCount(totalCount)
                .successRate(successRate)
                .avgDurationMs(avgDurationMs)
                .activeUserCount(activeUserCount)
                .totalUserCount(totalUserCount)
                .vipUserCount(vipUserCount)
                .quotaUsed(quotaUsed)
                .build();

        redisTemplate.opsForValue().set(STATISTICS_CACHE_KEY, statistics, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("统计数据已缓存，过期时间: {} 小时", CACHE_EXPIRE_HOURS);

        return statistics;
    }

    private Long countArticlesByDateRange(LocalDateTime start, LocalDateTime end) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .ge("createTime", start)
                .le("createTime", end);
        return articleMapper.selectCountByQuery(queryWrapper);
    }

    private Long countTotalArticles() {
        return articleMapper.selectCountByQuery(QueryWrapper.create());
    }

    private Double calculateSuccessRate() {
        Long totalCount = countTotalArticles();
        if (totalCount == 0) {
            return 0.0;
        }

        QueryWrapper successWrapper = QueryWrapper.create()
                .eq("status", ArticleStatusEnum.COMPLETED.getValue());
        Long successCount = articleMapper.selectCountByQuery(successWrapper);

        return (successCount.doubleValue() / totalCount.doubleValue()) * 100;
    }

    private Integer calculateAvgDuration() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", ArticleStatusEnum.COMPLETED.getValue())
                .isNotNull("completedTime");
        
        try {
            List<Article> completedArticles = articleMapper.selectListByQuery(queryWrapper);
            if (completedArticles == null || completedArticles.isEmpty()) {
                return 0;
            }

            double avgDuration = completedArticles.stream()
                    .filter(article -> article.getCreateTime() != null && article.getCompletedTime() != null)
                    .mapToLong(article -> {
                        long createMillis = java.sql.Timestamp.valueOf(article.getCreateTime()).getTime();
                        long completedMillis = java.sql.Timestamp.valueOf(article.getCompletedTime()).getTime();
                        return completedMillis - createMillis;
                    })
                    .average()
                    .orElse(0.0);

            return (int) avgDuration;
        } catch (Exception e) {
            log.warn("计算平均耗时失败", e);
        }
        
        return 0;
    }

    private Long countActiveUsers(LocalDateTime weekStart) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .ge("createTime", weekStart);
        
        try {
            List<Article> articles = articleMapper.selectListByQuery(queryWrapper);

                    .map(Article::getUserId)
                    .distinct()
                    .count();
        } catch (Exception e) {
            log.warn("统计活跃用户失败", e);
        }
        
return 0L;
    }

    private Long countTotalUsers() {
        return userMapper.selectCountByQuery(QueryWrapper.create());
    }

    private Long countVipUsers() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userRole", UserConstant.VIP_ROLE);
        return userMapper.selectCountByQuery(queryWrapper);
    }

    private Long calculateQuotaUsed() {
        QueryWrapper normalUserWrapper = QueryWrapper.create()
                .eq("userRole", UserConstant.DEFAULT_ROLE);
        
        try {
            List<User> normalUsers = userMapper.selectListByQuery(normalUserWrapper);
            Long normalUserCount = (long) normalUsers.size();
            
            long remainingQuota = normalUsers.stream()
                    .mapToInt(user -> user.getQuota() != null ? user.getQuota() : 0)
                    .sum();
            
            return (normalUserCount * UserConstant.DEFAULT_QUOTA) - remainingQuota;
        } catch (Exception e) {
            log.warn("计算配额使用量失�?, e);
        }
        
        return 0L;
    }

    private LocalDateTime getTodayStart() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    private LocalDateTime getWeekStart() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return LocalDateTime.of(monday, LocalTime.MIN);
    }

    private LocalDateTime getMonthStart() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        return LocalDateTime.of(firstDay, LocalTime.MIN);
    }
}

    
    private Long countVipUsers() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userRole", UserConstant.VIP_ROLE);
        return userMapper.selectCountByQuery(queryWrapper);
    }

    
    private Long calculateQuotaUsed() {

        QueryWrapper normalUserWrapper = QueryWrapper.create()
                .eq("userRole", UserConstant.DEFAULT_ROLE);
        
        try {
            List<User> normalUsers = userMapper.selectListByQuery(normalUserWrapper);
            Long normalUserCount = (long) normalUsers.size();

            long remainingQuota = normalUsers.stream()
                    .mapToInt(user -> user.getQuota() != null ? user.getQuota() : 0)
                    .sum();
            
            return (normalUserCount * UserConstant.DEFAULT_QUOTA) - remainingQuota;
        } catch (Exception e) {
            log.warn("计算配额使用量失�?, e);
        }
        
        return 0L;
    }

    
    private LocalDateTime getTodayStart() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    
    private LocalDateTime getWeekStart() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return LocalDateTime.of(monday, LocalTime.MIN);
    }

    
    private LocalDateTime getMonthStart() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        return LocalDateTime.of(firstDay, LocalTime.MIN);
    }
}
