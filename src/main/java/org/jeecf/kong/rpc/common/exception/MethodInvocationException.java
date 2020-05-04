package org.jeecf.kong.rpc.common.exception;

/**
 * 方法反射异常
 * 
 * @author jianyiming
 *
 */
public class MethodInvocationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MethodInvocationException(String message) {
        super(message);
    }

}
