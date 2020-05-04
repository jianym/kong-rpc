package org.jeecf.kong.rpc.common.exception;

/**
 * 参数不匹配异常
 * 
 * @author jianyiming
 *
 */
public class NotMatchArgsException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NotMatchArgsException(String message) {
        super(message);
    }

}
