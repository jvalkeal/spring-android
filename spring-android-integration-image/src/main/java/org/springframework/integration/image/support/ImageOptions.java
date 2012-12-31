/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.image.support;

import java.util.HashMap;
import java.util.Map;

/**
 * Options class representing what is a source of
 * target image, how images should be encoded and how
 * image should be processed into a bitmap.
 * 
 * @author Janne Valkealahti
 */
public class ImageOptions {

    /** Android resource id */
    private int mResourceId;
    /** Target url for image */
    private String mUrl;
    /** Image source */
    private ImageSource mImageSource;
    
    private Map<String, String> mTags;
    
    public ImageOptions() {
    }
    
    public ImageOptions(Builder builder) {
        mImageSource = builder.mImageSource;
        mUrl = builder.mUrl;
        mTags = builder.mTags;
        mResourceId = builder.mResourceId;
    }
    
    public ImageSource getImageSource() {
        return mImageSource;
    }
    
    public String getUrl() {
        return mUrl;
    }
    
    public int getResourceId() {
        return mResourceId;
    }
    
    public Map<String, String> getTags() {
        return mTags;
    }
    
    public static class Source {
        public ImageSource imageSource;
    }
    
    public enum ImageSource {
        HTTP,
        RESOURCE,
        ASSET
    }

    /**
     * Builder pattern to help construct ImageOptions instances.
     */
    public static class Builder {
        private int mResourceId;
        private String mUrl;
        private ImageSource mImageSource;
        private Map<String, String> mTags;
        
        public Builder withUrl(String url) {
            mUrl = url;
            mImageSource = ImageSource.HTTP;
            return this;
        }

        public Builder withResource(int id) {
            mResourceId = id;
            mImageSource = ImageSource.RESOURCE;
            return this;
        }

        public Builder withAsset(String path) {
            mImageSource = ImageSource.ASSET;
            return this;
        }
        
        public Builder addTag(String key, String value) {
            if(mTags == null) {
                mTags = new HashMap<String, String>();
            }
            mTags.put(key, value);
            return this;
        }
        
        public ImageOptions build() {
            return new ImageOptions(this);
        }
        
    }

}
