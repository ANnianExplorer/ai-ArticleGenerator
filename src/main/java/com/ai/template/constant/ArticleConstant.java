package com.ai.template.constant;

public interface ArticleConstant {

    long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    long SSE_RECONNECT_TIME_MS = 3000L;

    String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    int PEXELS_PER_PAGE = 1;

    String PEXELS_ORIENTATION_LANDSCAPE = "landscape";

    String PICSUM_URL_TEMPLATE = "https://picsum.photos/800/600?random=%d";

    String BING_IMAGE_SEARCH_URL = "https://cn.bing.com/images/async";

    String EMOJI_PACK_SUFFIX = "熊猫头表情包";

    int BING_MAX_IMAGES = 30;

    String SVG_FILE_PREFIX = "svg-chart";

    int SVG_DEFAULT_WIDTH = 800;

    int SVG_DEFAULT_HEIGHT = 600;

}