package org.jeecf.kong.rpc.common.exception;

/**
 * 没有服务连接异常
 * 
 * @author jianyiming
 *
 */
public class NoServerException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NoServerException(String message) {
        super(message);
    }

}
