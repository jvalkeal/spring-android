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

package org.springframework.integration.image.core;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapHttpMessageConverter implements HttpMessageConverter<Bitmap>{

    @Override
    public boolean canRead(Class<?> clazz, MediaType type) {
        return true;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType type) {
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.singletonList(new MediaType("image", "jpeg"));
    }

    @Override
    public Bitmap read(Class<? extends Bitmap> bitmap, HttpInputMessage message) throws IOException,
            HttpMessageNotReadableException {
        return BitmapFactory.decodeStream(message.getBody());
    }

    @Override
    public void write(Bitmap bitmap, MediaType type, HttpOutputMessage message) throws IOException,
            HttpMessageNotWritableException {
        throw new UnsupportedOperationException("Not implemented");
    }

    

}
