package org.fofaviewer.callback;

public interface SaveOptionCallback{
    default void setProjectName(String name){}
    default String getProjectName(){
        return null;
    }
}
