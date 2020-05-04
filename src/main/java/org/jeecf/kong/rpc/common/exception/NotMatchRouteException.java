package org.jeecf.kong.rpc.common.exception;

/**
 * 路由不匹配异常
 * 
 * @author jianyiming
 *
 */
public class NotMatchRouteException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NotMatchRouteException(String message) {
        super(message);
    }

}
