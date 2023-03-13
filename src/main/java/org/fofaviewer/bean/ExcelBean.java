package org.fofaviewer.bean;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
public class ExcelBean extends BaseBean {
    private String host;
    private String title;
    private String domain;

    private String certCN;

    private String ip;

    private Integer port;

    private String protocol;

    private String server;

    private String fid;

    public ExcelBean(String host, String title, String ip, String domain, Integer port, String protocol, String server, String fid, String certCN) {
        this.host = host;
        this.title = title;
        this.ip = ip;
        this.domain = domain;
        this.port = port;
        this.protocol = protocol;
        this.server = server;
        this.fid = fid;
        this.certCN = certCN;
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
