package org.fofaviewer.utils;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
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
        FofaConfig config = FofaConfig.getInstance();
        try (ExcelWriter excelWriter = ExcelUtil.getWriter(fileName, "查询结果")) {
            excelWriter.addHeaderAlias("host", "HOST");
            excelWriter.addHeaderAlias("title", "标题");
            excelWriter.addHeaderAlias("domain", "域名");
            excelWriter.addHeaderAlias("ip", "IP");
            excelWriter.addHeaderAlias("port", "端口");
            excelWriter.addHeaderAlias("protocol", "协议");
            excelWriter.addHeaderAlias("server", "server指纹");
            if(config.additionalField.contains("lastupdatetime")){
                excelWriter.addHeaderAlias("lastupdatetime", "最近更新时间");
            }
            if (config.additionalField.contains("os")) {
                excelWriter.addHeaderAlias("os", "操作系统");
            }
            if (config.additionalField.contains("icp")) {
                excelWriter.addHeaderAlias("icp", "ICP");
            }
            if (config.additionalField.contains("product")) {
                excelWriter.addHeaderAlias("product", "产品指纹");
            }
            if (config.additionalField.contains("fid")) {
                excelWriter.addHeaderAlias("fid", "fid");
            }
            if (config.additionalField.contains("certs_subject_cn")) {
                excelWriter.addHeaderAlias("certs_subject_cn", "证书域名");
            }
            if (config.additionalField.contains("certs_subject_org")) {
                excelWriter.addHeaderAlias("certs_subject_org", "证书持有者组织");
            }
            excelWriter.setOnlyAlias(true);
            // 设置表格格式
            excelWriter.autoSizeColumnAll();
            excelWriter.setColumnWidth(0, 30);
            excelWriter.setColumnWidth(1, 38);
            excelWriter.setColumnWidth(2, 20);
            excelWriter.setColumnWidth(3, 15);
            excelWriter.setColumnWidth(6, 20);
            excelWriter.setColumnWidth(7, 20);
            StyleSet style = excelWriter.getStyleSet();
            CellStyle cellStyle = style.getCellStyle();
            cellStyle.setWrapText(true);
            CellStyle headerStyle = style.getHeadCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font headerFont = excelWriter.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);
            excelWriter.setStyleSet(style);

            excelWriter.merge(6 + config.additionalField.size(), tabTitle, true);
            excelWriter.write(totalData, true);
            excelWriter.setSheet("urls");
            excelWriter.write(urls);
            excelWriter.setColumnWidth(0, 40);
            if(errorPage.length() == 0){
                showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_MESSAGE1") + fileName).showAndWait();
            }else{
                showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_MESSAGE2_1")
                        + errorPage + resourceBundle.getString("EXPORT_MESSAGE2_2") + " " + fileName).showAndWait();
            }
        } catch (Exception exception) {
            Logger.error(exception);
            showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_ERROR")).showAndWait();
        }
    }

    public static List<? extends BaseBean> loadJsonData(TabDataBean bean,
                                                               JSONObject obj,
                                                               List<ExcelBean> excelData,
                                                               HashSet<String> urlList,
                                                               boolean isExport){
        JSONArray array = obj.getJSONArray("results");
        List<TableBean> list = new ArrayList<>();
        if(array.isEmpty()){
            return list;
        }
        FofaConfig config = FofaConfig.getInstance();
        ArrayList<String> fileds = config.additionalField;
        for(int index=0; index < array.size(); index ++){
            JSONArray _array = array.getJSONArray(index);
            String host = _array.getString(0);
            String title= _array.getString(1);
            String ip = _array.getString(2);
            String domain = _array.getString(3);
            int port = Integer.parseInt(_array.getString(4));
            String protocol = _array.getString(5);
            if(port != 443 && port != 80 && (protocol.equals("http")||protocol.equals("https")) && !host.contains(":" + port)){
                continue;
            }
            String server = _array.getString(6);
            String link = _array.getString(7);
            HashMap<String, String> map = new HashMap<String, String>(){{
                put("fid","");put("os","");put("icp", "");put("product","");put("certs_subject_cn","");put("certs_subject_org","");put("lastupdatetime","");
            }};
            for(String item : map.keySet()){
                if(fileds.contains(item)){
                    map.put(item, _array.getString(8+fileds.indexOf(item)));
                }
            }
            if(isExport){ // 是否为导出数据
                ExcelBean d = new ExcelBean(host, title, ip, domain, port, protocol, server);
                d.setLastupdatetime(map.get("lastupdatetime"));
                d.setFid(map.get("fid"));
                d.setOs(map.get("os"));
                d.setProduct(map.get("product"));
                d.setIcp(map.get("icp"));
                d.setCerts_subject_cn(map.get("certs_subject_cn"));
                d.setCerts_subject_org(map.get("certs_subject_org"));
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
                        if(!d2.getTitle().isEmpty()){
                            continue;
                        }else {
                            excelData.remove(d2);
                        }
                    }
                }
                excelData.add(d);
                if(!link.isEmpty())
                    urlList.add(link);
            }else{  // table 页更新数据
                TableBean b = new TableBean(0, host, title, ip, domain, port, protocol, server);
                b.setFid(map.get("fid"));
                b.setIcp(map.get("icp"));
                b.setOs(map.get("os"));
                b.setCertCN(map.get("certs_subject_cn"));
                b.setProduct(map.get("product"));
                b.setCertOrg(map.get("certs_subject_org"));
                b.setLastUpdateTime(map.get("lastupdatetime"));
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
                        if(!b2.title.getValue().isEmpty()){
                            continue;
                        }else{
                            b.num = b2.num;
                            list.remove(b2);
                        }
                    }
                }
                if(b.num.getValue() == 0){ b.num.set(++bean.count);}
                if(!link.isEmpty())
                    bean.dataList.add(link);
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
