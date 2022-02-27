package org.fofaviewer.controls;

import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.fofaviewer.bean.TableBean;
import org.fofaviewer.callback.MainControllerCallback;
import org.fofaviewer.utils.DataUtil;
import org.fofaviewer.utils.RequestUtil;
import org.fofaviewer.utils.ResourceBundleUtil;
import org.tinylog.Logger;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * TableView装饰类
 */
public class MyTableView {
    private final ResourceBundle resourceBundle = ResourceBundleUtil.getResource();
    private final RequestUtil helper = RequestUtil.getInstance();
    
    public MyTableView(TableView<TableBean> view, MainControllerCallback mainControllerCallback) {
        TableColumn<TableBean, Integer> num = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_NO"));
        TableColumn<TableBean, String> host = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_HOST"));
        TableColumn<TableBean, String> title = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_TITLE"));
        TableColumn<TableBean, String> ip = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_IP"));
        TableColumn<TableBean, Integer> port = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_PORT"));
        TableColumn<TableBean, String> domain = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_DOMAIN"));
        TableColumn<TableBean, String> protocol = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_PROCOTOL"));
        TableColumn<TableBean, String> server = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_SERVER"));
        TableColumn<TableBean, String> fid = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_FID"));
        TableColumn<TableBean, String> cert = new TableColumn<>();
        TableColumn<TableBean, String> certCN = new TableColumn<>(resourceBundle.getString("TABLE_HEADER_CERTCN"));
        num.setCellValueFactory(param -> param.getValue().getNum().asObject());
        host.setCellValueFactory(param -> param.getValue().getHost());
        title.setCellValueFactory(param -> param.getValue().getTitle());
        ip.setCellValueFactory(param -> param.getValue().getIp());
        port.setCellValueFactory(param -> param.getValue().getPort().asObject());
        domain.setCellValueFactory(param -> param.getValue().getDomain());
        protocol.setCellValueFactory(param -> param.getValue().getProtocol());
        server.setCellValueFactory(param -> param.getValue().getServer());
        fid.setCellValueFactory(param -> param.getValue().getFid());
        cert.setCellValueFactory(param -> param.getValue().getCert());
        cert.setVisible(false); // 证书序列号太长默认不显示
        certCN.setCellValueFactory(param -> param.getValue().getCertCN());
        if(!mainControllerCallback.getFidStatus()){ // 未勾选fid时默认不显示
            fid.setVisible(false);
        }
        // 修改ip的排序规则
        ip.setComparator(Comparator.comparing(DataUtil::getValueFromIP));
        view.getColumns().add(num);
        view.getColumns().addAll(new ArrayList<TableColumn<TableBean,String>>(){{ add(host);add(title);add(ip);}});
        view.getColumns().add(port);
        view.getColumns().addAll(new ArrayList<TableColumn<TableBean,String>>(){{add(domain);add(protocol);add(certCN);add(server);add(fid);add(cert);}});
        view.setRowFactory(param -> {
            final TableRow<TableBean> row = new TableRow<>();
            // 设置表格右键菜单
            ContextMenu rowMenu = new ContextMenu();
            MenuItem copyLink = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_COPYLINK"));
            copyLink.setOnAction(event -> {
                //设置剪贴板
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(row.getItem().host.getValue());
                clipboard.setContent(content);
            });
            MenuItem copyIP = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_COPYIP"));
            copyIP.setOnAction(event -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(row.getItem().ip.getValue());
                clipboard.setContent(content);
            });
            MenuItem queryIp = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_IP"));
            queryIp.setOnAction(event -> {
                String _ip = row.getItem().ip.getValue();
                mainControllerCallback.queryCall("ip=\"" + _ip + "/32" + "\"");
            });
            MenuItem queryCSet = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_C-CLASS"));
            queryCSet.setOnAction(event -> {
                String _ip = row.getItem().ip.getValue();
                mainControllerCallback.queryCall("ip=\""+ _ip.substring(0, _ip.lastIndexOf('.')) + ".0/24" + "\"");
            });
            MenuItem querySubdomain = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_DOAMIN"));
            querySubdomain.setOnAction(event -> {
                String domain1 = row.getItem().domain.getValue();
                if(!domain1.isEmpty()){
                    mainControllerCallback.queryCall("domain=\""+ domain1 + "\"");
                }
            });
            MenuItem queryFavicon = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_FAVICON"));
            queryFavicon.setOnAction(event -> {
                String url = row.getItem().host.getValue();
                if(!url.startsWith("http")){
                    url = "http://" + url;
                    if(url.endsWith("443")){
                        url = "https://" + url.substring(0, url.length()-4);
                    }
                }
                String link = helper.getLinkIcon(url); // 请求获取link中的favicon链接
                HashMap<String,String> res;
                if(link !=null){
                    res = helper.getImageFavicon(link);
                }else{
                    res = helper.getImageFavicon(url + "/favicon.ico");
                }
                if(res != null){
                    if(res.get("code").equals("error")){
                        DataUtil.showAlert(Alert.AlertType.ERROR, null, res.get("msg")).showAndWait();return;
                    }
                    mainControllerCallback.queryCall(res.get("msg"));
                    return;
                }
                DataUtil.showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("QUERY_FAVICON_ERROR")).showAndWait();
            });
            MenuItem queryCert = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_CERT"));
            queryCert.setOnAction(event -> {
                String sn = row.getItem().cert.getValue();
                if(sn.isEmpty()){
                    DataUtil.showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("QUERY_CERT_ERROR")).showAndWait();
                }else{
                    mainControllerCallback.queryCall("cert=" + sn);
                }
            });
            MenuItem fidMenu = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_FID"));
            fidMenu.setOnAction(event -> {
                String _fid = row.getItem().fid.getValue();
                if(!_fid.isEmpty()){
                    mainControllerCallback.queryCall("fid=\""+_fid+"\"");
                }else{
                    DataUtil.showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("QUERY_FID_ERROR")).showAndWait();
                }
            });
            rowMenu.getItems().addAll(copyLink, copyIP, queryIp, queryCSet, querySubdomain, queryFavicon, queryCert, fidMenu);
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
            // 双击行时使用默认浏览器打开
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    String url = row.getItem().host.getValue();
                    String protocol1 = row.getItem().protocol.getValue();
                    String domain1 = row.getItem().domain.getValue();
                    if(!domain1.isEmpty() || url.startsWith("http") || protocol1.isEmpty() || protocol1.startsWith("http")){
                        if(!url.startsWith("http")){
                            if(url.endsWith("443")){
                                url = "https://" + url.substring(0, url.length()-4);
                            }
                            url = "http://" + url;
                        }
                        URI uri = URI.create(url);
                        Desktop dp = Desktop.getDesktop();
                        if (dp.isSupported(Desktop.Action.BROWSE)) {
                            // 获取系统默认浏览器打开链接
                            try {
                                dp.browse(uri);
                            } catch (IOException e) {
                                Logger.error(e);
                            }
                        }
                    }
                }
            });
            return row;
        });
        view.getSortOrder().add(num);
    }
}
