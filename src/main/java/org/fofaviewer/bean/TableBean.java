package org.fofaviewer.bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.Objects;

public class TableBean extends BaseBean{
    public SimpleIntegerProperty num = new SimpleIntegerProperty();
    public SimpleStringProperty host = new SimpleStringProperty();
    public SimpleStringProperty title = new SimpleStringProperty();
    public SimpleStringProperty ip = new SimpleStringProperty();
    public SimpleStringProperty domain = new SimpleStringProperty();
    public SimpleIntegerProperty port = new SimpleIntegerProperty();
    public SimpleStringProperty protocol = new SimpleStringProperty();
    public SimpleStringProperty server = new SimpleStringProperty();
    public SimpleStringProperty fid = new SimpleStringProperty();
    public SimpleStringProperty cert = new SimpleStringProperty();
    public SimpleStringProperty certCN = new SimpleStringProperty();
    public SimpleStringProperty status = new SimpleStringProperty();

    public TableBean(int num, String host, String title, String ip, String domain, int port, String protocol,
                     String server, String fid, String cert, String certCN) {
        this.num.set(num);
        this.host.set(host);
        this.title.set(title);
        this.ip.set(ip);
        this.domain.set(domain);
        this.port.set(port);
        this.protocol.set(protocol);
        this.server.set(server);
        this.fid.set(fid);
        this.cert.set(cert);
        this.certCN.set(certCN);
        this.status.set("");
    }

    public void setNum(SimpleIntegerProperty numValue){
        this.num = numValue;
    }

    public void setDomain(String value){
        this.domain = new SimpleStringProperty(value);
    }

    public int getIntNum(){
        return num.intValue();
    }

    public SimpleIntegerProperty getNum() {
        return num;
    }

    public SimpleStringProperty getHost() {
        return host;
    }

    public SimpleStringProperty getTitle() {
        return title;
    }

    public SimpleStringProperty getIp() {
        return ip;
    }

    public SimpleStringProperty getDomain() {
        return domain;
    }

    public SimpleIntegerProperty getPort() {
        return port;
    }

    public SimpleStringProperty getProtocol() {
        return protocol;
    }

    public SimpleStringProperty getServer(){
        return server;
    }

    public SimpleStringProperty getFid() {
        return fid;
    }

    public SimpleStringProperty getCert() {
        return cert;
    }

    public SimpleStringProperty getCertCN() {
        return certCN;
    }

    public SimpleStringProperty getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
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
