package com.season.example.transfer.receiver;

public interface OnWsListener {

    /**
     * 服务可用
     */
    void onServAvailable();

    /**
     * 服务不可用
     */
    void onServUnavailable();

}
