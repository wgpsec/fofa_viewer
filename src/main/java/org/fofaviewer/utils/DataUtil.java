package org.fofaviewer.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.fofaviewer.bean.BaseBean;
import org.fofaviewer.bean.ExcelBean;
import org.fofaviewer.bean.TabDataBean;
import org.fofaviewer.bean.TableBean;
import org.fofaviewer.controls.SetConfiDialog;
import org.fofaviewer.main.FofaConfig;
import org.fofaviewer.main.ProxyConfig;
import org.tinylog.Logger;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {
    private static final RequestUtil helper = RequestUtil.getInstance();
    private static final ResourceBundle resourceBundle = ResourceBundleUtil.getResource();
    private static final Pattern portPattern1 = Pattern.compile(":443$");
    private static final Pattern portPattern2 = Pattern.compile(":80$");

    /**
     * 对话框配置
     * @param type dialog type
     * @param header dialog title
     * @param content content of dialog
     */
    public static Alert showAlert(Alert.AlertType type, String header, String content){
        Alert alert = new Alert(type);
        alert.setTitle("提示");
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

    public static void exportToExcel(String fileName, String tabTitle, List<ExcelBean> totalData, List<List<String>> urls, StringBuilder errorPage) {
        ExcelWriter excelWriter = null;
        try{
            excelWriter = EasyExcel.write(fileName).withTemplate(DataUtil.class.getResourceAsStream("/template.xlsx")).build();
            WriteSheet writeSheet0 = EasyExcel.writerSheet(resourceBundle.getString("EXPORT_FILENAME_SHEET1")).build();
            Map<String, Object> map = new HashMap<>();
            map.put("title", tabTitle);
            excelWriter.fill(map, writeSheet0);
            excelWriter.fill(totalData, writeSheet0);
            WriteSheet writeSheet1 = EasyExcel.writerSheet(resourceBundle.getString("EXPORT_FILENAME_SHEET2")).build();
            excelWriter.write(urls, writeSheet1);
            if(errorPage.length() == 0){
                showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_MESSAGE1") + fileName).showAndWait();
            }else{
                showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_MESSAGE2_1")
                        + errorPage + resourceBundle.getString("EXPORT_MESSAGE2_2") + " " + fileName).showAndWait();
            }
        }catch(Exception exception){
            Logger.error(exception);
            showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_ERROR")).showAndWait();
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    public static void exportAllDataToExcel(String fileName){
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(fileName).build();

        } catch (Exception e){
            Logger.error(e);
        } finally {
            if(excelWriter != null){
                excelWriter.finish();
            }
        }
    }

    public static List<? extends BaseBean> loadJsonData(TabDataBean bean,
                                                               JSONObject obj,
                                                               List<ExcelBean> excelData,
                                                               HashSet<String> urlList,
                                                               boolean isExport){
        JSONArray array = obj.getJSONArray("results");
        List<TableBean>list = new ArrayList<>();
        if(array.size() == 0){
            return list;
        }
        FofaConfig config = FofaConfig.getInstance();
        for(int index=0; index < array.size(); index ++){
            JSONArray _array = array.getJSONArray(index);
            String host = _array.getString(config.fields.indexOf("host"));
            int port = Integer.parseInt(_array.getString(config.fields.indexOf("port")));
            String ip = _array.getString(config.fields.indexOf("ip"));
            String domain = _array.getString(config.fields.indexOf("domain"));
            String protocol = _array.getString(config.fields.indexOf("protocol"));
            if(port != 443 && port != 80 && (protocol.equals("http")||protocol.equals("https")) && !host.contains(":" + port)){
                continue;
            }

            String server = _array.getString(config.fields.indexOf("server"));
            String cert = "";
            String fid = "";
            String title = "";
            if(config.fields.contains("cert")){
                cert = _array.getString(config.fields.indexOf("cert"));
            }
            if(config.fields.contains("fid")){
                fid = _array.getString(config.fields.indexOf("fid"));
            }
            if(config.fields.contains("title")){
                title = _array.getString(config.fields.indexOf("title"));
            }
            String certCN = "";
            if(!cert.isEmpty()){
                certCN = helper.getCertSubjectDomainByFoFa(cert);
                cert = helper.getCertSerialNumberByFoFa(cert);
                if(domain.equals("") && !cert.equals("")){
                    int i = certCN.lastIndexOf(".");
                    int j = certCN.indexOf(".");
                    if(i > 0){
                        domain = i==j ? certCN : ( Character.isDigit(certCN.charAt(0)) ? "" : certCN.substring(j+1));
                    }else{
                        domain = "";
                    }
                }
            }
            if(isExport){ // 是否为导出数据
                ExcelBean d = new ExcelBean(host, title, ip, domain, port, protocol, server, fid, certCN);
                if(excelData.contains(d)){
                    ExcelBean d2 = excelData.get(excelData.indexOf(d));
                    if(port == 443 || port == 80){
                        if(d2.getHost().contains(":" + port)){
                            excelData.remove(d2);
                        }else if(d.getHost().contains(":" + port)){
                            continue;
                        }
                        if(d2.getHost().equals(d.getHost())){
                            excelData.remove(d2);
                        }
                    }
                    if(d2.getHost().equals(d.getHost())){
                        if(!d2.getTitle().equals("")){
                            continue;
                        }else {
                            excelData.remove(d2);
                        }
                    }
                }
                excelData.add(d);
                getUrlList(urlList, host, protocol);
            }else{  // table 页更新数据
                TableBean b = new TableBean(0, host, title, ip, domain, port, protocol, server, fid, cert, certCN);
                if(list.contains(b)){
                    TableBean b2 = list.get(list.indexOf(b));
                    if(port == 443 || port == 80){
                        // host 带有额外的 443 或 80
                        if(b2.host.getValue().contains(":" + port)){
                            b.num = b2.num;
                            list.remove(b2);
                        }else if(b.host.getValue().contains(":" + port)){
                            continue;
                        }
                    }
                    // host 相同时 去掉不带title的
                    if(b2.host.getValue().equals(b.host.getValue())){
                        if(!b2.title.getValue().equals("")){
                            continue;
                        }else{
                            b.num = b2.num;
                            list.remove(b2);
                        }
                    }
                }
                if(b.num.getValue() == 0){ b.num.set(++bean.count);}
                getUrlList(bean.dataList, host, protocol);
                list.add(b);
            }
        }
        return list;
    }

    public static void getUrlList(HashSet<String> urlList, String host, String protocol) {
        if(protocol.startsWith("http") || protocol.equals("tls")){
            Matcher m1 = portPattern1.matcher(host);
            Matcher m2 = portPattern2.matcher(host);
            if(host.startsWith("http://") || host.startsWith("https://")) {
                urlList.add(host);
            }else if(protocol.equals("tls")){
                urlList.add("https://" + host);
            }else if(m1.find()){
                urlList.add(protocol + "://" + host.substring(0, host.indexOf(":443")));
            }else if(m2.find()){
                urlList.add(protocol + "://" + host.substring(0, host.indexOf(":80")));
            }else{
                urlList.add(protocol + "://" + host);
            }
        }
    }

    /**
     * 将IP地址转换为浮点数
     * @param ip IP
     * @return double value
     */
    public static Double getValueFromIP(String ip){
        String[] str = ip.split("\\.");
        return Double.parseDouble(str[0]) * 1000000 + Double.parseDouble(str[1]) * 1000
                + Double.parseDouble(str[2]) + Double.parseDouble(str[3]) * 0.001;
    }

    public static String replaceString(String tabTitle){
        if(tabTitle.startsWith("(*)")){
            tabTitle = tabTitle.substring(3);
            tabTitle = "(" + tabTitle + ") && (is_honeypot=false && is_fraud=false)";
        }
        return tabTitle;
    }

    /**
     * 从配置文件加载fofa认证信息
     */
    public static FofaConfig loadConfigure(){
        Properties properties = new Properties();
        FofaConfig client;
        ProxyConfig proxyConfig;
        try {
            properties.load(new FileReader(SQLiteUtils.getPath() + "config.properties"));
            client = FofaConfig.getInstance();
            client.setEmail(properties.getProperty("email").trim());
            client.setKey(properties.getProperty("key").trim());
            client.setAPI(properties.getProperty("api").trim());
            client.setSize(properties.getProperty("max_size"));
            client.setCheckStatus(properties.getProperty("check_status").equals("on"));
            proxyConfig = ProxyConfig.getInstance();
            if(properties.getProperty("proxy_status").equals("on")){
                proxyConfig.setStatus(true);
                switch(properties.getProperty("proxy_type")){
                    case "HTTP": proxyConfig.setProxy_type(ProxyConfig.ProxyType.HTTP);break;
                    case "SOCKS5": proxyConfig.setProxy_type(ProxyConfig.ProxyType.SOCKS5);break;
                }
                proxyConfig.setProxy_ip(properties.getProperty("proxy_ip"));
                proxyConfig.setProxy_port(properties.getProperty("proxy_port"));
                proxyConfig.setProxy_user(properties.getProperty("proxy_user"));
                proxyConfig.setProxy_password(properties.getProperty("proxy_password"));
            }else{
                proxyConfig.setStatus(false);
            }
        } catch (IOException | NullPointerException e){
            Logger.error(e);
            setConfigDialog();
            client = FofaConfig.getInstance();
        }
        return client;
    }

    private static void setConfigDialog(){
        Alert dialog = showAlert(Alert.AlertType.CONFIRMATION, null, resourceBundle.getString("LOAD_CONFIG_ERROR"));
        dialog.setOnCloseRequest(event -> {
            ButtonType result = dialog.getResult();
            if(result.getButtonData() == ButtonBar.ButtonData.OK_DONE){
                SetConfiDialog scf = new SetConfiDialog(resourceBundle.getString("CONFIG_PANEL"));
                scf.setOnCloseRequest(event1 -> {
                    if(scf.getResult() == ButtonType.CANCEL){
                        Platform.exit();
                    }
                });
                scf.showAndWait();
            }else{
                Platform.exit();//结束进程
            }
        });
        dialog.showAndWait();
    }
}
