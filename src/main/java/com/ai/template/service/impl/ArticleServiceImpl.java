package com.ai.template.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ai.template.exception.BusinessException;
import com.ai.template.exception.ErrorCode;
import com.ai.template.exception.ThrowUtils;
import com.ai.template.mapper.ArticleMapper;
import com.ai.template.model.dto.article.ArticleQueryRequest;
import com.ai.template.model.dto.article.ArticleState;
import com.ai.template.model.entity.Article;
import com.ai.template.model.entity.User;
import com.ai.template.model.enums.ArticlePhaseEnum;
import com.ai.template.model.enums.ArticleStatusEnum;
import com.ai.template.model.vo.ArticleVO;
import com.ai.template.service.ArticleAgentService;
import com.ai.template.service.ArticleService;
import com.ai.template.service.QuotaService;
import com.ai.template.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.ai.template.model.enums.ImageMethodEnum;

import static com.ai.template.constant.UserConstant.ADMIN_ROLE;
import static com.ai.template.constant.UserConstant.VIP_ROLE;

@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private QuotaService quotaService;

    @Resource
    private ArticleAgentService articleAgentService;

    @Override
    public String createArticleTask(String topic, String style, List<String> enabledImageMethods, User loginUser) {

        List<String> finalImageMethods = processImageMethods(enabledImageMethods, loginUser);


        String taskId = IdUtil.simpleUUID();

        Article article = new Article();
        article.setTaskId(taskId);
        article.setUserId(loginUser.getId());
        article.setTopic(topic);
        article.setStyle(style);
        article.setEnabledImageMethods(finalImageMethods != null && !finalImageMethods.isEmpty() 
                ? GsonUtils.toJson(finalImageMethods) : null);
        article.setStatus(ArticleStatusEnum.PENDING.getValue());
        article.setPhase(ArticlePhaseEnum.PENDING.getValue());
        article.setCreateTime(LocalDateTime.now());

        this.save(article);

        log.info("文章任务已创�? taskId={}, userId={}, style={}", taskId, loginUser.getId(), style);
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createArticleTaskWithQuotaCheck(String topic, String style, List<String> enabledImageMethods, User loginUser) {


        quotaService.checkAndConsumeQuota(loginUser);
        return createArticleTask(topic, style, enabledImageMethods, loginUser);
    }

    @Override
    public Article getByTaskId(String taskId) {
        return this.getOne(
                QueryWrapper.create().eq("taskId", taskId)
        );
    }

    @Override
    public ArticleVO getArticleDetail(String taskId, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存�?);


        return ArticleVO.objToVo(article);
    }

    @Override
    public Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, User loginUser) {
        long current = request.getPageNum();
        long size = request.getPageSize();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("isDelete", 0)
                .orderBy("createTime", false);

            queryWrapper.eq("userId", loginUser.getId());
        } else if (request.getUserId() != null) {
            queryWrapper.eq("userId", request.getUserId());
        }

            queryWrapper.eq("status", request.getStatus());
        }

        Page<Article> articlePage = this.page(new Page<>(current, size), queryWrapper);

        return convertToVOPage(articlePage);
    }

    @Override
    public boolean deleteArticle(Long id, User loginUser) {
        Article article = this.getById(id);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR);


        return this.removeById(id);
    }

    @Override
    public void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage) {
        Article article = getByTaskId(taskId);

        if (article == null) {
            log.error("文章记录不存�? taskId={}", taskId);
            return;
        }

        article.setStatus(status.getValue());
        article.setErrorMessage(errorMessage);
        this.updateById(article);

        log.info("文章状态已更新, taskId={}, status={}", taskId, status.getValue());
    }

    @Override
    public void saveArticleContent(String taskId, ArticleState state) {
        Article article = getByTaskId(taskId);

        if (article == null) {
            log.error("文章记录不存�? taskId={}", taskId);
            return;
        }

        article.setMainTitle(state.getTitle().getMainTitle());
        article.setSubTitle(state.getTitle().getSubTitle());
        article.setOutline(GsonUtils.toJson(state.getOutline().getSections()));
        article.setContent(state.getContent());
        article.setFullContent(state.getFullContent());

            ArticleState.ImageResult cover = state.getImages().stream()
                .filter(img -> img.getPosition() != null && img.getPosition() == 1)
                .findFirst()
                .orElse(null);
            if (cover != null && cover.getUrl() != null) {
                article.setCoverImage(cover.getUrl());
            }
        }
        article.setImages(GsonUtils.toJson(state.getImages()));
        article.setCompletedTime(LocalDateTime.now());

        this.updateById(article);
        log.info("文章保存成功, taskId={}", taskId);
    }

    private void checkArticlePermission(Article article, User loginUser) {
        if (!article.getUserId().equals(loginUser.getId()) &&
                !ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    private Page<ArticleVO> convertToVOPage(Page<Article> articlePage) {
        Page<ArticleVO> articleVOPage = new Page<>();
        articleVOPage.setPageNumber(articlePage.getPageNumber());
        articleVOPage.setPageSize(articlePage.getPageSize());
        articleVOPage.setTotalRow(articlePage.getTotalRow());

        List<ArticleVO> articleVOList = articlePage.getRecords().stream()
                .map(ArticleVO::objToVo)
                .collect(Collectors.toList());
        articleVOPage.setRecords(articleVOList);

        return articleVOPage;
    }

    @Override
    public void confirmTitle(String taskId, String mainTitle, String subTitle, String userDescription, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存�?);

        checkArticlePermission(article, loginUser);

        ThrowUtils.throwIf(currentPhase != ArticlePhaseEnum.TITLE_SELECTING,
                ErrorCode.OPERATION_ERROR, "当前阶段不允许此操作");

        article.setMainTitle(mainTitle);
        article.setSubTitle(subTitle);
        article.setUserDescription(userDescription);
        article.setPhase(ArticlePhaseEnum.OUTLINE_GENERATING.getValue());

        this.updateById(article);
        log.info("用户确认标题, taskId={}, mainTitle={}", taskId, mainTitle);
    }

    @Override
    public void confirmOutline(String taskId, List<ArticleState.OutlineSection> outline, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存�?);

        checkArticlePermission(article, loginUser);

        ThrowUtils.throwIf(currentPhase != ArticlePhaseEnum.OUTLINE_EDITING,
                ErrorCode.OPERATION_ERROR, "当前阶段不允许此操作");

        article.setOutline(GsonUtils.toJson(outline));
        article.setPhase(ArticlePhaseEnum.CONTENT_GENERATING.getValue());

        this.updateById(article);
        log.info("用户确认大纲, taskId={}, sectionsCount={}", taskId, outline.size());
    }

    @Override
    public void updatePhase(String taskId, ArticlePhaseEnum phase) {
        Article article = getByTaskId(taskId);
        if (article == null) {
            log.error("文章记录不存�? taskId={}", taskId);
            return;
        }

        article.setPhase(phase.getValue());
        this.updateById(article);
        log.info("文章阶段已更�? taskId={}, phase={}", taskId, phase.getValue());
    }

    @Override
    public void saveTitleOptions(String taskId, List<ArticleState.TitleOption> titleOptions) {
        Article article = getByTaskId(taskId);
        if (article == null) {
            log.error("文章记录不存�? taskId={}", taskId);
            return;
        }

        article.setTitleOptions(GsonUtils.toJson(titleOptions));
        this.updateById(article);
        log.info("标题方案已保�? taskId={}, optionsCount={}", taskId, titleOptions.size());
    }

    @Override
    public List<ArticleState.OutlineSection> aiModifyOutline(String taskId, String modifySuggestion, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存�?);

        checkArticlePermission(article, loginUser);

                "AI 修改大纲功能仅限 VIP 会员使用");

        ThrowUtils.throwIf(currentPhase != ArticlePhaseEnum.OUTLINE_EDITING,
                ErrorCode.OPERATION_ERROR, "当前阶段不允许此操作");

        List<ArticleState.OutlineSection> currentOutline = GsonUtils.fromJson(
                article.getOutline(),
                new TypeToken<List<ArticleState.OutlineSection>>(){}
        );

        List<ArticleState.OutlineSection> modifiedOutline = articleAgentService.aiModifyOutline(
                article.getMainTitle(),
                article.getSubTitle(),
                currentOutline,
                modifySuggestion
        );

        article.setOutline(GsonUtils.toJson(modifiedOutline));
        this.updateById(article);

        log.info("AI修改大纲完成, taskId={}, sectionsCount={}", taskId, modifiedOutline.size());
        return modifiedOutline;
    }

    private List<String> processImageMethods(List<String> enabledImageMethods, User loginUser) {

            return enabledImageMethods;
        }

            return null;
        }

        return List.of(
                ImageMethodEnum.PEXELS.getValue(),
                ImageMethodEnum.MERMAID.getValue(),
                ImageMethodEnum.ICONIFY.getValue(),
                ImageMethodEnum.EMOJI_PACK.getValue()
        );
    }

    private void validateImageMethods(List<String> enabledImageMethods, User loginUser) {
        if (enabledImageMethods == null || enabledImageMethods.isEmpty()) {
            return;
        }

            return;
        }

        for (String method : enabledImageMethods) {
            if (ImageMethodEnum.NANO_BANANA.getValue().equals(method) || 
                ImageMethodEnum.SVG_DIAGRAM.getValue().equals(method)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, 
                        "高级配图功能（AI 生图、SVG 图表）仅�?VIP 会员使用");
            }
        }
    }

    private boolean isVipOrAdmin(User user) {
        return ADMIN_ROLE.equals(user.getUserRole()) || 
               VIP_ROLE.equals(user.getUserRole());
    }
}
