package org.fofaviewer.bean;

import javafx.beans.property.SimpleStringProperty;

public class TableBean {
    public SimpleStringProperty num = new SimpleStringProperty();
    public SimpleStringProperty host = new SimpleStringProperty();
    public SimpleStringProperty title = new SimpleStringProperty();
    public SimpleStringProperty ip = new SimpleStringProperty();
    public SimpleStringProperty domain = new SimpleStringProperty();
    public SimpleStringProperty port = new SimpleStringProperty();
    public SimpleStringProperty protocol = new SimpleStringProperty();

    public TableBean(String num, String host, String title, String ip, String domain, String port, String protocol) {
        this.num.set(num);
        this.host.set(host);
        this.title.set(title);
        this.ip.set(ip);
        this.domain.set(domain);
        this.port.set(port);
        this.protocol.set(protocol);
    }

    public SimpleStringProperty getNum() {
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

    public SimpleStringProperty getPort() {
        return port;
    }

    public SimpleStringProperty getProtocol() {
        return protocol;
    }
}
