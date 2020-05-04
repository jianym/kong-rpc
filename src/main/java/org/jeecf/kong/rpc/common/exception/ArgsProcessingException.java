package org.jeecf.kong.rpc.common.exception;

/**
 * 参数异常
 * 
 * @author jianyiming
 *
 */
public class ArgsProcessingException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ArgsProcessingException(String message) {
        super(message);
    }
}
