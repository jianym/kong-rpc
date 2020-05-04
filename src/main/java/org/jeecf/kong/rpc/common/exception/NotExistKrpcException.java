package org.jeecf.kong.rpc.common.exception;

/**
 * krpc 注解不存在异常
 * 
 * @author jianyiming
 *
 */
public class NotExistKrpcException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NotExistKrpcException(String message) {
        super(message);
    }

}
