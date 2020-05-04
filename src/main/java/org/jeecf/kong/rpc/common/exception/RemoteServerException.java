package org.jeecf.kong.rpc.common.exception;

/**
 * 远程服务异常
 * 
 * @author jianyiming
 *
 */
public class RemoteServerException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RemoteServerException(String message) {
        super(message);
    }

}
