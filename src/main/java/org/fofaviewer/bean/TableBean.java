package org.fofaviewer.bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class TableBean extends BaseBean{
    @Setter
    @Getter
    public SimpleIntegerProperty num = new SimpleIntegerProperty();
    @Getter
    public SimpleStringProperty host = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty title = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty ip = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty domain = new SimpleStringProperty();
    @Getter
    public SimpleIntegerProperty port = new SimpleIntegerProperty();
    @Getter
    public SimpleStringProperty protocol = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty server = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty lastupdatetime = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty fid = new SimpleStringProperty();
    public SimpleStringProperty os = new SimpleStringProperty();
    public SimpleStringProperty icp = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty product = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty certCN = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty certOrg = new SimpleStringProperty();
    @Getter
    public SimpleStringProperty status = new SimpleStringProperty();

    public TableBean(int num, String host, String title, String ip, String domain, int port, String protocol, String server) {
        this.num.set(num);
        this.host.set(host);
        this.title.set(title);
        this.ip.set(ip);
        this.domain.set(domain);
        this.port.set(port);
        this.protocol.set(protocol);
        this.server.set(server);
    }

    public void setFid(String fid) {
        this.fid = new SimpleStringProperty(fid);
    }

    public void setOs(String os) {
        this.os = new SimpleStringProperty(os);
    }

    public void setIcp(String icp) {
        this.icp = new SimpleStringProperty(icp);
    }

    public void setProduct(String product) {
        this.product = new SimpleStringProperty(product);
    }

    public void setCertCN(String certCN) {
        this.certCN = new SimpleStringProperty(certCN);
    }

    public void setCertOrg(String certOrg) {
        this.certOrg = new SimpleStringProperty(certOrg);
    }

    public void setDomain(String value){
        this.domain = new SimpleStringProperty(value);
    }

    public void setLastUpdateTime(String lastupdatetime) {
        this.lastupdatetime = new SimpleStringProperty(lastupdatetime);
    }

    public int getIntNum(){
        return num.intValue();
    }



    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TableBean))
            return false;
        if (this == obj)
            return true;
        TableBean instance = (TableBean) obj;
        boolean bool_host = this.host.getValue().equals(instance.host.getValue());
        boolean bool_port = this.port.getValue().equals(instance.port.getValue());
        if(bool_port){
            if(this.port.getValue() == 443 && (this.host.getValue().contains(":443") || instance.host.getValue().contains(":443"))){
                bool_host = true;
            }
            if(this.port.getValue() == 80 && (this.host.getValue().contains(":80") || instance.host.getValue().contains(":80"))){
                bool_host = true;
            }
        }
        boolean bool_ip = this.ip.getValue().equals(instance.ip.getValue());
        return bool_host && bool_ip && bool_port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip.getValue(), port.getValue());
    }
}
