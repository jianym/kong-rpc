package org.jeecf.kong.rpc.common.exception;

import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 响应异常工具
 * 
 * @author jianyiming
 *
 */
public class ResponseExceptionUtils {

    public static final int SUCCESS = 0;

    public static final int ARGS_PROCESSING_CODE = 101;

    public static final int METHOD_INVOCATION_CODE = 102;

    public static final int RESOURCE_LOCATION_CODE = 103;

    public static final int NOT_KNOW_CODE = 500;

    public static void throwException(int code, String message) {
        if (code == SUCCESS) {
            return;
        } else if (code == ARGS_PROCESSING_CODE) {
            throw new ArgsProcessingException(message);
        } else if (code == METHOD_INVOCATION_CODE) {
            throw new MethodInvocationException(message);
        } else if (code == RESOURCE_LOCATION_CODE) {
            throw new ResourceLocationNotMatchException(message);
        } else {
            throw new RemoteServerException(message);
        }
    }

    public static int getCode(Throwable e) {
        if (e instanceof IllegalAccessException) {
            return METHOD_INVOCATION_CODE;
        } else if (e instanceof IllegalArgumentException) {
            return ARGS_PROCESSING_CODE;
        } else if (e instanceof ArgsProcessingException) {
            return ARGS_PROCESSING_CODE;
        } else if (e instanceof InvocationTargetException) {
            return METHOD_INVOCATION_CODE;
        } else if (e instanceof JsonProcessingException) {
            return ARGS_PROCESSING_CODE;
        } else {
            return NOT_KNOW_CODE;
        }
    }

}
