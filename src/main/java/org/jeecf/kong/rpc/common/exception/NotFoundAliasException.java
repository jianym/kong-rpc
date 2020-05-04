package org.jeecf.kong.rpc.common.exception;

/**
 * 别名不匹配异常
 * 
 * @author jianyiming
 *
 */
public class NotFoundAliasException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NotFoundAliasException(String message) {
        super(message);
    }

}
