package org.fofaviewer.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestStatus {
    RUNNING("查询中"), FAILED("查询失败"), SUCCEEDED("查询成功"),READY("准备");

    private final String msg;

    @Override
    public String toString(){
        return getMsg();
    }
}
