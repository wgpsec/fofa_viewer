package org.fofaviewer.request;

import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.StatusBar;
import org.fofaviewer.bean.RequestBean;
import org.fofaviewer.bean.TabDataBean;

/**
 * 设置请求的回调
 */
public interface RequestCallback<T> {
    /**
     * 请求之前先添加tab
     */
    default void before(TabDataBean tabDataBean){}
    /**
     * 请求成功
     */
    default void succeeded(BorderPane tablePane, StatusBar bar){}

    /**
     * 请求失败
     */
    default void failed(RequestBean bean){}

    /**
     * 暂停线程
     */
    default void pause(RequestBean bean){}

}
