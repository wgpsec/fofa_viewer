package org.fofaviewer.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

@Getter
@Data
public class ExcelBean extends BaseBean {
    private String host;
    private String title;
    private String domain;
    private String ip;
    private Integer port;
    private String protocol;
    private String server;
    @Setter
    private String lastupdatetime;
    @Setter
    private String fid;
    @Setter
    private String os;
    @Setter
    private String icp;
    @Setter
    private String product;
    @Setter
    private String certs_subject_org;
    @Setter
    private String certs_subject_cn;

    public ExcelBean(String host, String title, String ip, String domain, Integer port, String protocol, String server) {
        this.host = host;
        this.title = title.length() > 32767 ? title.substring(0,200) + "......(标题长度超出限额，截断处理)" : title;
        this.ip = ip;
        this.domain = domain;
        this.port = port;
        this.protocol = protocol;
        this.server = server;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExcelBean))
            return false;
        if (this == obj)
            return true;
        ExcelBean instance = (ExcelBean) obj;
        boolean port = this.port.equals(instance.port);
        boolean host = this.host.equals(instance.host);
        if(port){
            if(this.port == 443 && (this.host.contains(":443") || instance.host.contains(":443"))){
                host = true;
            }
            if(this.port == 80 && (this.host.contains(":80") || instance.host.contains(":80"))){
                host = true;
            }
        }
        boolean ip = this.ip.equals(instance.ip);

        return host && ip && port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
