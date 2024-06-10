package org.fofaviewer.callback;

import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.List;

/**
 * MainController回调，用于在线程中设置调用MainController的方法
 */
public interface MainControllerCallback {

    default HashMap<String, Boolean> getCheckBoxStatus() {
        return null;
    }

    default void queryCall(List<String> strList){}

    default void addSBListener(TableView<?> view){}

    default void setStatusBar(){}
}
