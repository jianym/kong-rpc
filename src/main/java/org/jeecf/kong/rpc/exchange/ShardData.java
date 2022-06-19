package org.jeecf.kong.rpc.exchange;

/**
 * 碎片传输数据
 * 
 * @author jianyiming
 *
 */
public class ShardData {
    /**
     * 参数的json数据
     */
    private String jsonData;
    /**
     * 客户端发送标识
     */
    private String clientId;
    /**
     * 关闭状态 false 发送中 ，true 发送完成
     */
    private boolean close = false;

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

}
