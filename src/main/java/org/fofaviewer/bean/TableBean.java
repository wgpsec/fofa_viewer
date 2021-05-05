package org.fofaviewer.bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableBean {
    public SimpleIntegerProperty num = new SimpleIntegerProperty();
    public SimpleStringProperty host = new SimpleStringProperty();
    public SimpleStringProperty title = new SimpleStringProperty();
    public SimpleStringProperty ip = new SimpleStringProperty();
    public SimpleStringProperty domain = new SimpleStringProperty();
    public SimpleIntegerProperty port = new SimpleIntegerProperty();
    public SimpleStringProperty protocol = new SimpleStringProperty();
    public SimpleStringProperty server = new SimpleStringProperty();

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

}
