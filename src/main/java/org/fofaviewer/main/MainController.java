package org.fofaviewer.main;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.fofaviewer.bean.FofaBean;
import org.fofaviewer.bean.TableBean;
import org.fofaviewer.utils.LogUtil;
import org.fofaviewer.utils.RequestHelper;
import org.fofaviewer.controls.CloseableTabPane;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 处理事件
 */
public class MainController {

    @FXML
    private VBox rootLayout;
    @FXML
    private TextField queryTF;
    @FXML
    private CloseableTabPane tabPane;
    private final RequestHelper helper = RequestHelper.getInstance();
    private FofaBean client;
    private Logger logger = null;


    public MainController(){
        this.logger = Logger.getLogger("Controller");
        LogUtil.setLogingProperties(this.logger);
    }
    /**
     * 初始化
     */
    @FXML
    private void initialize(){
        loadConfigure();
        Tab tab = this.tabPane.getTab("启动页");
        Button queryCert = new Button("计算");
        Label label = new Label("证书序列号计算器");
        Label resLabel = new Label("结果");
        TextField tf = new TextField();
        TextField res = new TextField();
        tf.setPromptText("请将16进制证书序列号粘贴到此处！");
        tf.setPrefWidth(400);
        res.setPrefWidth(500);
        label.setFont(Font.font(14));
        resLabel.setFont(Font.font(14));
        queryCert.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(tf.getText() != null){
                    String txt = tf.getText().trim().replaceAll(" ", "");
                    BigInteger i = new BigInteger(txt, 16);
                    res.setText(i.toString());
                }
            }
        });
        VBox vb = new VBox();
        HBox hb = new HBox();
        HBox res_hb = new HBox();
        hb.getChildren().add(label);
        hb.getChildren().add(tf);
        hb.getChildren().add(queryCert);
        label.setPadding(new Insets(3));
        resLabel.setPadding(new Insets(3));
        hb.setPadding(new Insets(20));
        hb.setSpacing(10);
        res_hb.setSpacing(45);
        hb.setAlignment(Pos.TOP_CENTER);
        res_hb.setAlignment(Pos.TOP_CENTER);
        res_hb.getChildren().add(resLabel);
        res_hb.getChildren().add(res);
        vb.getChildren().add(hb);
        vb.getChildren().add(res_hb);
        VBox.setMargin(res, new Insets(0, 100, 0 ,100));
        tab.setContent(vb);
    }

    /**
     * 查询按钮点击事件，与fxml中命名绑定
     * @param event 点击事件
     */
    @FXML
    private void queryAction(ActionEvent event){
        if(queryTF.getText() != null){
            query(queryTF.getText().toString());
        }
    }

    @FXML
    private void showAbout(ActionEvent event){
        showAlert(Alert.AlertType.INFORMATION, null, "项目地址：https://github.com/wgpsec/fofa_viewer");
    }

    /**
     * 查询条件回车监听
     * @param e 键盘事件
     */
    @FXML
    private void queryEnter(KeyEvent e){
        if(e.getCode() == KeyCode.ENTER) {
            query(queryTF.getText().toString());
        }
    }

    /**
     * 导出查询数据
     */
    @FXML
    private void exportAction(ActionEvent e) {
        if(tabPane.getCurrentTab().getText().equals("起始页")) return;  // 起始页无数据可导出
        HashSet<String> set = new HashSet<>(this.tabPane.getList(tabPane.getCurrentTab())); // 去重
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择导出结果到文件...");
        try {
            File file = fileChooser.showSaveDialog(stage);
            if(file != null){
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for(String i : set){
                    writer.write(i);
                    writer.newLine();
                }
                writer.close();
            }
        }catch (IOException ex){
            logger.log(Level.WARNING,ex.getMessage(), ex);
            showAlert(Alert.AlertType.ERROR, null, "导出失败，错误原因：" + ex.getMessage());
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, null, "导出成功！");
    }

    /**
     * 处理查询结果
     */
    public void query(String text){
        text = text.trim();
        if(this.tabPane.isExistTab(text)){ // 若已存在同名Tab 则直接跳转，不查询
            this.tabPane.setCurrentTab(this.tabPane.getTab(text));
            return;
        }
        text = RequestHelper.encode(text);
        HashMap<String,String> result = this.helper.getHTML(this.client.getParam() + text);
        if(result.get("code").equals("200")){
            Tab tab = new Tab();
            ArrayList<String> dataList = new ArrayList<>();
            JSONObject obj = JSON.parseObject(result.get("msg"));
            if(obj.getBoolean("error")){
                showAlert(Alert.AlertType.ERROR, null, obj.getString("errmsg"));
                return;
            }
            List<TableBean> list = new ArrayList<>();
            JSONArray array = obj.getJSONArray("results");
            for(int index=0; index < array.size(); index ++){
                String protocol = array.getJSONArray(index).getString(5);
                String domain = array.getJSONArray(index).getString(4);
                String host = array.getJSONArray(index).getString(0);
                TableBean b = new TableBean(
                    String.valueOf(index + 1),
                    host,
                    array.getJSONArray(index).getString(1),
                    array.getJSONArray(index).getString(2),
                    array.getJSONArray(index).getString(3),
                    domain, protocol
                );
                list.add(b);
                if(protocol.equals("http") || protocol.equals("https") || !domain.equals("")){
                    if(host.startsWith("https") && !dataList.contains(host.substring(8))){
                        dataList.add(array.getJSONArray(index).getString(0));
                        continue;
                    }
                    dataList.add(array.getJSONArray(index).getString(0));
                }
            }
            String query_title = obj.getString("query");
            tab.setText(query_title);
            this.tabPane.addTab(tab, dataList);
            BorderPane tablePane = new BorderPane();
            TableView<TableBean> view = new TableView<>(FXCollections.observableArrayList(list));
            setTable(view);
            tablePane.setCenter(view);
            tab.setContent(tablePane);
            tabPane.setCurrentTab(tab);
        }else{
            showAlert(Alert.AlertType.ERROR, result.get("code"), result.get("msg"));
        }
    }

    /**
     * 从配置文件加载fofa认证信息
     */
    public void loadConfigure(){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
            this.client = new FofaBean(properties.getProperty("email"), properties.getProperty("key"));
            this.client.setSize(properties.getProperty("maxSize"));
        } catch (IOException e){
            showAlert(Alert.AlertType.ERROR, null, "缺少配置文件config.properties！");
            Platform.exit();  //结束进程
        }
    }

    /**
     * 表格配置
     */
    public void setTable(TableView<TableBean> view){
        TableColumn<TableBean, String> num = new TableColumn<>("序号");
        TableColumn<TableBean, String> host = new TableColumn<>("HOST");
        TableColumn<TableBean, String> title = new TableColumn<>("标题");
        TableColumn<TableBean, String> ip = new TableColumn<>("IP");
        TableColumn<TableBean, String> port = new TableColumn<>("端口");
        TableColumn<TableBean, String> domain = new TableColumn<>("域名");
        TableColumn<TableBean, String> protocol = new TableColumn<>("协议");
        List<TableColumn<TableBean,String>> list = new ArrayList<TableColumn<TableBean,String>>(){{
            add(num);add(host);add(title);add(ip);add(port);add(domain);add(protocol);
        }};
        num.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableBean, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<TableBean, String> param) {
                return param.getValue().getNum();
            }
        });
        host.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableBean, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<TableBean, String> param) {
                return param.getValue().getHost();
            }
        });

        title.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableBean, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<TableBean, String> param) {
                return param.getValue().getTitle();
            }
        });

        ip.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableBean, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<TableBean, String> param) {
                return param.getValue().getIp();
            }
        });

        port.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableBean, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<TableBean, String> param) {
                return param.getValue().getPort();
            }
        });

        domain.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableBean, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<TableBean, String> param) {
                return param.getValue().getDomain();
            }
        });

        protocol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableBean, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<TableBean, String> param) {
                return param.getValue().getProtocol();
            }
        });

        view.getColumns().addAll(list);
        view.setRowFactory(new Callback<TableView<TableBean>, TableRow<TableBean>>() {
            @Override
            public TableRow<TableBean> call(TableView<TableBean> param) {
                final TableRow<TableBean> row = new TableRow<>();
                // 设置表格右键菜单
                ContextMenu rowMenu = new ContextMenu();
                MenuItem copyLink = new MenuItem("复制链接");
                copyLink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) { //设置剪贴板
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(row.getItem().host.getValue());
                        clipboard.setContent(content);
                    }
                });
                MenuItem queryCSet = new MenuItem("查询对应C段资产");
                queryCSet.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String ip = row.getItem().ip.getValue();
                        String queryText = ip.substring(0, ip.lastIndexOf('.'));
                        query("ip=\""+ queryText + ".1/24" + "\"");
                    }
                });
                MenuItem querySubdomain = new MenuItem("查询相关域名资产");
                querySubdomain.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String domain = row.getItem().domain.getValue();
                        if(!domain.equals("")){
                            query("domain=\""+ domain + "\"");
                        }
                    }
                });
                MenuItem favicon = new MenuItem("从fofa搜索favicon相关的资产");
                favicon.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String url = row.getItem().host.getValue();
                        if(!url.startsWith("http")){
                            url = "http://" + url;
                            if(url.endsWith("443")){
                                url = "https://" + url.substring(0, url.length()-4);
                            }
                        }
                        String link = helper.getLinkIcon(url); // 请求获取link中的favicon链接
                        HashMap<String,String> res = null;
                        if(link !=null){
                            res = helper.getImageFavicon(link);
                        }else{
                            res = helper.getImageFavicon(url + "/favicon.ico");
                        }
                        if(res != null){
                            if(res.get("code").equals("error")){
                                showAlert(Alert.AlertType.ERROR, null, res.get("msg"));return;
                            }
                            query(res.get("msg"));
                            return;
                        }
                        showAlert(Alert.AlertType.ERROR, null, "该网站未提供favicon");
                    }
                });
                MenuItem cert = new MenuItem("从fofa搜索证书相关的资产");
                cert.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String host = row.getItem().host.getValue();
                        if(host.startsWith("https")){
                            try {
                                String value = helper.getCertSerialNum(host);
                                if(value != null){
                                    query(value);
                                }
                            }catch (Exception e){
                                logger.log(Level.WARNING, e.getMessage(), e);
                            }
                        }else{
                            showAlert(Alert.AlertType.WARNING, null, "请选中host中带有https的行再点击查询证书相关资产");
                        }
                    }
                });
                rowMenu.getItems().addAll(copyLink, queryCSet, querySubdomain, favicon, cert);
                row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
                // 双击行时使用默认浏览器打开
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                            String url = row.getItem().host.getValue();
                            String protocol = row.getItem().protocol.getValue();
                            String domain = row.getItem().domain.getValue();
                            if(protocol.startsWith("http") || !domain.equals("") || url.startsWith("http")){
                                if(!url.startsWith("http")){
                                    if(url.endsWith("443")){
                                        url = "https://" + url.substring(0, url.length()-4);
                                    }
                                    url = "http://" + url;
                                }
                                java.net.URI uri = java.net.URI.create(url);
                                java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                                if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                                    // 获取系统默认浏览器打开链接
                                    try {
                                        dp.browse(uri);
                                    } catch (IOException e) {
                                        logger.log(Level.WARNING, e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                });
                return row;
            }
        });
    }

    /**
     * 对话框配置
     * @param type 对话框类型
     * @param header 对话框标题
     * @param content 对话框内容
     */
    public void showAlert(Alert.AlertType type, String header, String content){
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
