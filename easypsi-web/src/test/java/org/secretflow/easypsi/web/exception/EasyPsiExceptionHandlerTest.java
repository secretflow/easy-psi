/*
 * Copyright 2024 Ant Group Co., Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.secretflow.easypsi.common.errorcode.ErrorCode;
import org.secretflow.easypsi.common.errorcode.SystemErrorCode;
import org.secretflow.easypsi.common.exception.EasyPsiException;
import org.secretflow.easypsi.common.i18n.MessageResolver;
import org.secretflow.easypsi.service.model.common.EasyPsiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;


import static org.mockito.Mockito.*;

/**
 * @author chenmingliang
 * @date 2024/03/13
 */
public class EasyPsiExceptionHandlerTest {
    @InjectMocks
    private EasyPsiExceptionHandler easyPsiExceptionHandler;

    @Mock
    private MessageResolver messageResolver;


    @Mock
    private BindException bindException;

    private MethodArgumentNotValidException methodArgumentNotValidException;

    private Exception exception;

    @BeforeEach
    public void setUp() throws NoSuchMethodException {
        MockitoAnnotations.openMocks(this);

        // 创建 BeanPropertyBindingResult 对象，模拟绑定结果
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objectName");
        FieldError fieldError = new FieldError("objectName", "field", "default message");
        bindingResult.addError(fieldError);

        MethodParameter methodParameter = new MethodParameter(
                Object.class.getDeclaredMethod("toString"), -1);

        methodArgumentNotValidException = new MethodArgumentNotValidException(methodParameter, bindingResult);

        exception = new Exception("Test exception");

    }

    @Test
    public void testHandlerEasyPsiException() {
        EasyPsiException easyPsiException = mock(EasyPsiException.class);
        ErrorCode errorCode = SystemErrorCode.UNKNOWN_ERROR; // Example ErrorCode
        String errorMessage = "Error message";

        when(easyPsiException.getErrorCode()).thenReturn(errorCode);
        when(messageResolver.getMessage(Mockito.any(ErrorCode.class), Mockito.any())).thenReturn(errorMessage);

        EasyPsiResponse<Object> result = easyPsiExceptionHandler.handlerEasyPsiException(easyPsiException);

        verify(messageResolver).getMessage(errorCode, easyPsiException.getArgs());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(errorCode.getCode(), result.getStatus().getCode());
        Assertions.assertEquals(errorMessage, result.getStatus().getMsg());
    }

    @Test
    public void testHandlerValidation() {
        String errorMessage = "Validation Error Message";
        ErrorCode errorCode = SystemErrorCode.VALIDATION_ERROR;

        when(messageResolver.getMessage(any(ErrorCode.class), any())).thenReturn(errorMessage);

        EasyPsiResponse<Object> response = (EasyPsiResponse<Object>) easyPsiExceptionHandler.handlerValidation(methodArgumentNotValidException);

        verify(messageResolver).getMessage(any(ErrorCode.class), any());
        Assertions.assertNotNull(response);
        Assertions.assertEquals(errorCode.getCode(), response.getStatus().getCode());
        Assertions.assertEquals(errorMessage, response.getStatus().getMsg());
    }

    @Test
    public void testHandleException() {
        String resolvedErrorMessage = "An unknown error has occurred.";
        when(messageResolver.getMessage(any(SystemErrorCode.class), any())).thenReturn(resolvedErrorMessage);

        Object result = easyPsiExceptionHandler.handleException(exception);

        Assertions.assertNotNull(result);
        EasyPsiResponse<?> response = (EasyPsiResponse<?>) result;

        Assertions.assertEquals(SystemErrorCode.UNKNOWN_ERROR.getCode(), response.getStatus().getCode());
        Assertions.assertEquals(resolvedErrorMessage, response.getStatus().getMsg());

        verify(messageResolver).getMessage(any(SystemErrorCode.class), any());
    }

}
