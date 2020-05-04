package org.jeecf.kong.rpc.common.exception;

/**
 * 连接超时异常
 * 
 * @author jianyiming
 *
 */
public class TimeoutException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TimeoutException() {
        super("server socket timeout...");
    }

    public TimeoutException(String message) {
        super(message);
    }

}
