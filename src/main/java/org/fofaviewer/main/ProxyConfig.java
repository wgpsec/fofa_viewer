package org.fofaviewer.main;

import me.gv7.woodpecker.requests.Proxies;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public class ProxyConfig {
    public enum ProxyType {HTTP, SOCKS5 }
    private static ProxyConfig config;
    private boolean status;
    private ProxyType proxy_type;
    private String proxy_ip;
    private String proxy_port;
    private String proxy_user;
    private String proxy_password;

    private ProxyConfig() {
        status = false;
        proxy_type = ProxyType.HTTP;
        proxy_ip = "";
        proxy_port = "";
        proxy_password = "";
        proxy_user = "";
    }
    public static ProxyConfig getInstance(){
        if (config == null) {
            config = new ProxyConfig();
        }
        return config;
    }

    public Proxy getProxy() {
        if(!this.proxy_user.equals("") && !this.proxy_password.equals("")){
            Authenticator.setDefault(new Authenticator(){
                public PasswordAuthentication getPasswordAuthentication() {
                    return (new PasswordAuthentication(proxy_user, proxy_password.toCharArray()));
                }
            });
        }else{
            Authenticator.setDefault(null);
        }
        switch (this.proxy_type){
            case HTTP: return Proxies.httpProxy(this.proxy_ip, Integer.parseInt(this.proxy_port));
            case SOCKS5: return Proxies.socksProxy(this.proxy_ip, Integer.parseInt(this.proxy_port));
        }
        return null;
    }

    public void setProxy_type(ProxyType proxy_type) {
        this.proxy_type = proxy_type;
    }

    public String getProxy_ip() {
        return proxy_ip;
    }

    public void setProxy_ip(String proxy_ip) {
        this.proxy_ip = proxy_ip;
    }

    public String getProxy_port() {
        return proxy_port;
    }

    public void setProxy_port(String proxy_port) {
        this.proxy_port = proxy_port;
    }

    public String getProxy_user() {
        return proxy_user;
    }

    public void setProxy_user(String proxy_user) {
        this.proxy_user = proxy_user;
    }

    public String getProxy_password() {
        return proxy_password;
    }

    public void setProxy_password(String proxy_password) {
        this.proxy_password = proxy_password;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
