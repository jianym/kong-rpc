package org.jeecf.kong.rpc.common.exception;

/**
 * engine不存在异常
 * 
 * @author jianyiming
 *
 */
public class NotExistSslEngineException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NotExistSslEngineException(String message) {
        super(message);
    }

}
