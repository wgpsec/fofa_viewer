package org.fofaviewer.bean;
import java.util.HashSet;

public class TabDataBean {
    /**
     * 查询条件对应tab页中已加载的数量
     */
    public int count;
    /**
     * 查询条件对应tab页可获取的总数
     */
    public int total;
    /**
     * 导出查询条件对应tab页中的url，用于后续扫描
     */
    public HashSet<String> dataList;
    /**
     * 是否还有未加载的数据
     */
    public boolean hasMoreData = true;
    /**
     * 当前已加载的页数
     */
    public int page = 1;

    public TabDataBean(int count, HashSet<String> dataList) {
        this.count = count;
        this.dataList = dataList;
    }
    public TabDataBean(){
        this.count = 0;
        this.dataList = new HashSet<>();
    }
}
