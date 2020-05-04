package org.jeecf.kong.rpc.common.exception;

/**
 * 通信 异常
 * 
 * @author jianyiming
 *
 */
public class SocketException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SocketException(String message) {
        super(message);
    }

}
