package org.fofaviewer.bean;

import java.util.Map;

public class FofaBean {
    public String email;
    public String key;
    public  String page = "1";
    private String size = "1000";
    private final String fields = "host,title,ip,domain,port,protocol";
    private final String queryUrl = "https://fofa.so/api/v1/search/all";

    public FofaBean(String m, String k) {
        this.email = m;
        this.key = k;
    }

    public void setSize(String size){
        this.size = size;
    }
    public String getParam() {
        return queryUrl + "?email=" + email
                + "&key=" + key
                + "&page=" + page
                + "&size=" + size
                + "&fields=" + fields
                + "&qbase64=";
    }
}
