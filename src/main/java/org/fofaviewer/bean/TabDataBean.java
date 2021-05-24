package org.fofaviewer.bean;

import java.util.ArrayList;

public class TabDataBean {
    public int count;
    public ArrayList<String> dataList;
    public boolean hasMoreData = true;
    public int page = 1;

    public TabDataBean(int count, ArrayList<String> dataList) {
        this.count = count;
        this.dataList = dataList;
    }
    public TabDataBean(){
        this.count = 0;
        this.dataList = new ArrayList<>();
    }
}
