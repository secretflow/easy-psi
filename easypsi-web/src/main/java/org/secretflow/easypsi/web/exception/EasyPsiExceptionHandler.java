/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.easypsi.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.secretflow.easypsi.common.errorcode.ErrorCode;
import org.secretflow.easypsi.common.errorcode.SystemErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.i18n.MessageResolver;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.secretflow.easypsi.web.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyPsi exception handler
 *
 * @author yansi
 * @date 2023/5/10
 */
@RestControllerAdvice
public class EasyPsiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EasyPsiExceptionHandler.class);

    @Autowired
    private MessageResolver messageResolver;

    /**
     * Catch EasyPsiException and build EasyPsiResponse with errorCode and message
     *
     * @param ex EasyPsiException
     * @return EasyPsiResponse with errorCode and message
     */
    @ExceptionHandler(value = EasyPsiException.class)
    public EasyPsiResponse<Object> handlerEasyPsiException(EasyPsiException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String message = messageResolver.getMessage(errorCode, ex.getArgs());
        LOGGER.error("find error: {}, message: {} ", ex.getErrorCode(), ex.getMessage());
        return new EasyPsiResponse<>(new EasyPsiResponse.EasyPsiResponseStatus(errorCode.getCode(), message),
                null);
    }

    /**
     * Catch MethodArgumentNotValidException and build EasyPsiResponse with validation error and message
     *
     * @param ex MethodArgumentNotValidException
     * @return EasyPsiResponse with validation error and message
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object handlerValidation(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> collect = new ArrayList<>();
        for (FieldError fieldError : fieldErrors) {
            String defaultMessage = fieldError.getDefaultMessage();
            collect.add(defaultMessage);
        }
        LOGGER.error("handler argument check error: {}, cause: {}", ex.getMessage(), ex.getCause());
        return handlerEasyPsiException(EasyPsiException.of(SystemErrorCode.VALIDATION_ERROR, collect.get(0)));
    }

    /**
     * Catch Exception and build EasyPsiResponse with unknown error and message
     *
     * @param ex Exception
     * @return EasyPsiResponse with unknown error and message
     */
    @ExceptionHandler(value = Exception.class)
    public Object handleException(Exception ex) {
        LOGGER.error("handler error: {}, cause: {}, ex: {}", ex.getMessage(), ex.getCause(), ex);
        return handlerEasyPsiException(EasyPsiException.of(SystemErrorCode.UNKNOWN_ERROR, ex.getMessage()));
    }

    /**
     * Catch FileSizeLimitExceededException and build EasyPsiResponse with unknown error and message
     *
     * @param ex FileSizeLimitExceededException
     * @return EasyPsiResponse with unknown error and message
     */
    @ExceptionHandler(value = FileSizeLimitExceededException.class)
    public Object handleFileSizeException(Exception ex) {
        LOGGER.error("handler error: {}, cause: {}, ex: {}", ex.getMessage(), ex.getCause(), ex);
        return handlerEasyPsiException(EasyPsiException.of(SystemErrorCode.UNKNOWN_ERROR, ex.getMessage()));
    }

    /**
     * Catch handlerMethodNotSupportedException and build response
     *
     * @param response
     * @param request
     */

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public void handlerMethodNotSupportedException(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ResponseUtils.buildResponse_404(response, request);
    }

    /**
     * Catch handlerNoHandlerFoundException and build response
     *
     * @param response
     * @param request
     */

    @ExceptionHandler(value = NoHandlerFoundException.class)
    public void handlerNoHandlerFoundException(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ResponseUtils.buildResponse_404(response, request);
    }

    /**
     * capture additional exception information
     */
    @ExceptionHandler(value = DataAccessResourceFailureException.class)
    public void DataAccessResourceFailureException() {
        LOGGER.error("capture additional exception information: {}", DataAccessResourceFailureException.class);
    }

}
