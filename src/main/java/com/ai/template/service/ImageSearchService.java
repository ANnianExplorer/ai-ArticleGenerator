package com.ai.template.service;

import com.ai.template.model.dto.image.ImageData;
import com.ai.template.model.dto.image.ImageRequest;
import com.ai.template.model.enums.ImageMethodEnum;


public interface ImageSearchService {

    
    default String getImage(ImageRequest request) {

        String param = request.getEffectiveParam(getMethod().isAiGenerated());
        return searchImage(param);
    }

    
    default ImageData getImageData(ImageRequest request) {

        String url = getImage(request);
        return ImageData.fromUrl(url);
    }

    
    String searchImage(String keywords);

    
    ImageMethodEnum getMethod();

    
    String getFallbackImage(int position);

    
    default boolean isAvailable() {
        return true;
    }
}
