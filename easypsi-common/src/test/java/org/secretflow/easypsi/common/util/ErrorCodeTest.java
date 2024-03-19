package org.secretflow.easypsi.common.util;

import org.secretflow.easypsi.common.errorcode.ErrorCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Method;
import java.util.*;

/**
 * ErrorCode test
 *
 * @author xujiening
 * @date 2023/08/17
 */
public class ErrorCodeTest {

    /**
     * ErrorCode list in overall situation
     */
    private List<Integer> errorCodeList = new ArrayList<>();


    /**
     * Check error code if repeated
     *
     * @throws Exception
     */
    @Test
    public void checkErrorCodeIfRepeated() throws Exception {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(ErrorCode.class));
        // scan in error code package
        Set<BeanDefinition> components = provider.findCandidateComponents("org.secretflow.easypsi.common.errorcode");
        for (BeanDefinition component : components) {
            Class<?> cls = Class.forName(component.getBeanClassName());
            // class enum constants
            for (Object enumConstant : cls.getEnumConstants()) {
                Method getCode = enumConstant.getClass().getDeclaredMethod("getCode");
                Integer errorCode = Integer.parseInt(getCode.invoke(enumConstant).toString());
                if (errorCodeList.contains(errorCode)) {
                    String errMsg = String.format("ErrorCode[%d] is repeated, please redefine!", errorCode);
                    throw new RuntimeException(errMsg);
                }
                errorCodeList.add(errorCode);
            }
        }
    }

    /**
     * Check error code if exists in I18n file
     *
     * @throws Exception
     */
    @Test
    public void checkErrorCodeIfExistInI18nFile() throws Exception {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(ErrorCode.class));
        // scan in error code package
        Set<BeanDefinition> components = provider.findCandidateComponents("org.secretflow.easypsi.common.errorcode");
        // US i18n file
        ResourceBundle resourceBundleUS = ResourceBundle.getBundle("i18n.messages", Locale.US);
        // CN i18n file
        ResourceBundle resourceBundleCN = ResourceBundle.getBundle("i18n.messages", Locale.CHINA);
        for (BeanDefinition component : components) {
            Class<?> cls = Class.forName(component.getBeanClassName());
            // class enum constants
            for (Object enumConstant : cls.getEnumConstants()) {
                String enumString = enumConstant.toString();
                // get message key
                Method getMessageKey = enumConstant.getClass().getDeclaredMethod("getMessageKey");
                String messageKey = getMessageKey.invoke(enumConstant).toString();
                if (!resourceBundleUS.containsKey(messageKey)) {
                    String errMsg = String.format("ErrorCode[%s] does not exist in US I18n file , please check!", enumString);
                    throw new RuntimeException(errMsg);
                }
                if (!resourceBundleCN.containsKey(messageKey)) {
                    String errMsg = String.format("ErrorCode[%s] does not exist in CN I18n file , please check!", enumString);
                    throw new RuntimeException(errMsg);
                }
            }
        }
    }
}
