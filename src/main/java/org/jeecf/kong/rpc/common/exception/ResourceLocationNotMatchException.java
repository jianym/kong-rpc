package org.jeecf.kong.rpc.common.exception;

/**
 * 资源定位异常
 * 
 * @author jianyiming
 *
 */
public class ResourceLocationNotMatchException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ResourceLocationNotMatchException(String message) {
        super(message);
    }

}
