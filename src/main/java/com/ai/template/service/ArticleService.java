package com.ai.template.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.ai.template.model.dto.article.ArticleQueryRequest;
import com.ai.template.model.dto.article.ArticleState;
import com.ai.template.model.entity.Article;
import com.ai.template.model.entity.User;
import com.ai.template.model.enums.ArticlePhaseEnum;
import com.ai.template.model.enums.ArticleStatusEnum;
import com.ai.template.model.vo.ArticleVO;

import java.util.List;

public interface ArticleService extends IService<Article> {

    String createArticleTask(String topic, String style, List<String> enabledImageMethods, User loginUser);

    String createArticleTaskWithQuotaCheck(String topic, String style, List<String> enabledImageMethods, User loginUser);

    Article getByTaskId(String taskId);

    ArticleVO getArticleDetail(String taskId, User loginUser);

    Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, User loginUser);

    boolean deleteArticle(Long id, User loginUser);

    void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage);

    void saveArticleContent(String taskId, ArticleState state);

    void confirmTitle(String taskId, String mainTitle, String subTitle, String userDescription, User loginUser);

    void confirmOutline(String taskId, List<ArticleState.OutlineSection> outline, User loginUser);

    void updatePhase(String taskId, ArticlePhaseEnum phase);

    void saveTitleOptions(String taskId, List<ArticleState.TitleOption> titleOptions);

    List<ArticleState.OutlineSection> aiModifyOutline(String taskId, String modifySuggestion, User loginUser);
}
