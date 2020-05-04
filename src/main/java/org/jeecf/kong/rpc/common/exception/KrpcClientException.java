package org.jeecf.kong.rpc.common.exception;

/**
 * krpcclient 异常
 * 
 * @author jianyiming
 *
 */
public class KrpcClientException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public KrpcClientException(Throwable cause) {
        super(cause);
    }

}
