package com.ai.template.controller;

import com.mybatisflex.core.paginate.Page;
import com.ai.template.common.BaseResponse;
import com.ai.template.common.DeleteRequest;
import com.ai.template.common.ResultUtils;
import com.ai.template.exception.ErrorCode;
import com.ai.template.exception.ThrowUtils;
import com.ai.template.manager.SseEmitterManager;
import com.ai.template.model.dto.article.ArticleAiModifyOutlineRequest;
import com.ai.template.model.dto.article.ArticleConfirmOutlineRequest;
import com.ai.template.model.dto.article.ArticleConfirmTitleRequest;
import com.ai.template.model.dto.article.ArticleCreateRequest;
import com.ai.template.model.dto.article.ArticleQueryRequest;
import com.ai.template.model.dto.article.ArticleState;

import java.util.List;
import com.ai.template.model.entity.User;
import com.ai.template.model.enums.ArticleStyleEnum;
import com.ai.template.model.vo.AgentExecutionStats;
import com.ai.template.model.vo.ArticleVO;
import com.ai.template.service.AgentLogService;
import com.ai.template.service.ArticleAsyncService;
import com.ai.template.service.ArticleService;
import com.ai.template.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/article")
@Slf4j
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleAsyncService articleAsyncService;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private UserService userService;

    @Resource
    private AgentLogService agentLogService;

    @PostMapping("/create")
    @Operation(summary = "Create article task")
    public BaseResponse<String> createArticle(@RequestBody ArticleCreateRequest request, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTopic() == null || request.getTopic().trim().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "Topic cannot be empty");
        ThrowUtils.throwIf(!ArticleStyleEnum.isValid(request.getStyle()),
                ErrorCode.PARAMS_ERROR, "Invalid article style");

        User loginUser = userService.getLoginUser(httpServletRequest);

        String taskId = articleService.createArticleTaskWithQuotaCheck(
                request.getTopic(), 
                request.getStyle(), 
                request.getEnabledImageMethods(),
                loginUser
        );

        articleAsyncService.executePhase1(
                taskId, 
                request.getTopic(),
                request.getStyle()
        );

        return ResultUtils.success(taskId);
    }

    @GetMapping("/progress/{taskId}")
    @Operation(summary = "Get article generation progress (SSE)")
    public SseEmitter getProgress(@PathVariable String taskId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);
        articleService.getArticleDetail(taskId, loginUser);

        SseEmitter emitter = sseEmitterManager.createEmitter(taskId);
        
        log.info("SSE connection established, taskId={}", taskId);
        return emitter;
    }

    
    @GetMapping("/{taskId}")
    @Operation(summary = "Get article detail")
    public BaseResponse<ArticleVO> getArticle(@PathVariable String taskId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);
        ArticleVO articleVO = articleService.getArticleDetail(taskId, loginUser);

        return ResultUtils.success(articleVO);
    }

    @PostMapping("/list")
    @Operation(summary = "Paginated query article list")
    public BaseResponse<Page<ArticleVO>> listArticle(@RequestBody ArticleQueryRequest request,
                                                       HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Page<ArticleVO> articleVOPage = articleService.listArticleByPage(request, loginUser);
        
        return ResultUtils.success(articleVOPage);
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete article")
    public BaseResponse<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest,
                                                 HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, 
                ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = articleService.deleteArticle(deleteRequest.getId(), loginUser);
        
        return ResultUtils.success(result);
    }

    @PostMapping("/confirm-title")
    @Operation(summary = "Confirm title and enter supplementary description")
    public BaseResponse<Void> confirmTitle(@RequestBody ArticleConfirmTitleRequest request,
                                            HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTaskId() == null || request.getTaskId().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");
        ThrowUtils.throwIf(request.getSelectedMainTitle() == null || request.getSelectedMainTitle().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Main title cannot be empty");
        ThrowUtils.throwIf(request.getSelectedSubTitle() == null || request.getSelectedSubTitle().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Sub title cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);

        articleService.confirmTitle(
                request.getTaskId(),
                request.getSelectedMainTitle(),
                request.getSelectedSubTitle(),
                request.getUserDescription(),
                loginUser
        );

        articleAsyncService.executePhase2(request.getTaskId());

        return ResultUtils.success(null);
    }

    @PostMapping("/confirm-outline")
    @Operation(summary = "Confirm outline")
    public BaseResponse<Void> confirmOutline(@RequestBody ArticleConfirmOutlineRequest request,
                                              HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTaskId() == null || request.getTaskId().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");
        ThrowUtils.throwIf(request.getOutline() == null || request.getOutline().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Outline cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);

        articleService.confirmOutline(
                request.getTaskId(),
                request.getOutline(),
                loginUser
        );

        articleAsyncService.executePhase3(request.getTaskId());

        return ResultUtils.success(null);
    }

    @PostMapping("/ai-modify-outline")
    @Operation(summary = "AI modify outline")
    public BaseResponse<List<ArticleState.OutlineSection>> aiModifyOutline(
            @RequestBody ArticleAiModifyOutlineRequest request,
            HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTaskId() == null || request.getTaskId().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");
        ThrowUtils.throwIf(request.getModifySuggestion() == null || request.getModifySuggestion().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Modification suggestion cannot be empty");

        User loginUser = userService.getLoginUser(httpServletRequest);

        List<ArticleState.OutlineSection> modifiedOutline = articleService.aiModifyOutline(
                request.getTaskId(),
                request.getModifySuggestion(),
                loginUser
        );

        return ResultUtils.success(modifiedOutline);
    }

    @GetMapping("/execution-logs/{taskId}")
    @Operation(summary = "Get task execution logs")
    public BaseResponse<AgentExecutionStats> getExecutionLogs(@PathVariable String taskId) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "Task ID cannot be empty");
        
        AgentExecutionStats stats = agentLogService.getExecutionStats(taskId);
        return ResultUtils.success(stats);
    }
}