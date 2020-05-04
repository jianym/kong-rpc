package org.jeecf.kong.rpc.common.exception;

/**
 * 资源格式异常
 * 
 * @author jianyiming
 *
 */
public class ResourceLocationFormatException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ResourceLocationFormatException(String message) {
        super(message);
    }

}
