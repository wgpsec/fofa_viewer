package org.fofaviewer.bean;

import java.util.Locale;

public class FofaBean {
    public String email;
    public String key;
    public String page = "1";
    public final int max = 10000;
    public String size = "1000";
    public String API = "https://fofa.info";
    public final String path = "/api/v1/search/all";
    public static final String TIP_API = "https://api.fofa.info/v1/search/tip?q=";
    //public static final String TIP_API = Locale.getDefault()==Locale.CHINA ? "https://api.fofa.so/v1/search/tip?q="
    public static final String fields = "host,title,ip,domain,port,protocol,server,cert";
    public static final String fid = "fid";

    public FofaBean(String m, String k, String a) {
        this.email = m;
        this.key = k;
        this.API = a + this.path;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size){
        this.size = size;
    }

    public String getParam(String page, boolean hasFid, boolean isAll) {
        String fields = hasFid ? FofaBean.fields + "," +fid : FofaBean.fields;
        String all = isAll ? "&full=true" : "";
        StringBuilder builder = new StringBuilder(API).append("?email=").append(email).append("&key=").append(key).append(all).append("&page=");
        if(page != null) {
            return builder.append(page).append("&size=").append(size).append("&fields=").append(fields).append("&qbase64=").toString();
        }else{
            return builder.append(this.page).append("&size=").append(size).append("&fields=").append(fields).append("&qbase64=").toString();
        }
    }
}
