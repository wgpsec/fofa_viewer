package org.fofaviewer.main;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.merge.OnceAbsoluteMergeStrategy;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.StatusBar;
import org.controlsfx.dialog.CommandLinksDialog;
import org.controlsfx.dialog.ProgressDialog;
import org.fofaviewer.bean.*;
import org.fofaviewer.controls.AutoHintTextField;
import org.fofaviewer.utils.LogUtil;
import org.fofaviewer.utils.RequestHelper;
import org.fofaviewer.controls.CloseableTabPane;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 处理事件
 */
public class MainController {
    @FXML
    private VBox rootLayout;
    @FXML
    private TextField queryTF;
    @FXML
    private CheckBox checkHoneyPot;
    @FXML
    private CloseableTabPane tabPane;
    private AutoHintTextField decoratedField;
    private static final RequestHelper helper = RequestHelper.getInstance();
    private static FofaBean client;
    private Logger logger = null;

    public MainController(){
        this.logger = Logger.getLogger("Controller");
        LogUtil.setLogingProperties(this.logger);
    }
    /**
     * 初始化
     */
    @FXML
    private void initialize() throws IOException {
        decoratedField = new AutoHintTextField(queryTF);
        loadConfigure();
        //初始化起始页tab
        Tab tab = this.tabPane.getTab("首页");
        Button queryCert = new Button("计算");
        Label label = new Label("证书序列号计算器");
        Label resLabel = new Label("结果");
        TextField tf = new TextField();
        TextField res = new TextField();
        Image image = new Image("api_doc.png");
        ImageView view = new ImageView(image);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(view);
        view.setFitHeight(2000D);
        view.setFitWidth(750D);
        scrollPane.setPrefWidth(760);
        tf.setPromptText("请将16进制证书序列号粘贴到此处！");
        tf.setPrefWidth(400);
        res.setPrefWidth(500);
        label.setFont(Font.font(14));
        resLabel.setFont(Font.font(14));
        queryCert.setOnAction(event -> {
            if(tf.getText() != null){
                String txt = tf.getText().trim().replaceAll(" ", "");
                BigInteger i = new BigInteger(txt, 16);
                res.setText(i.toString());
            }
        });
        VBox vb = new VBox();
        HBox hb = new HBox();
        HBox resBox = new HBox();
        HBox imageBox = new HBox();
        vb.setSpacing(10);
        imageBox.getChildren().add(scrollPane);
        hb.getChildren().addAll(label, tf, queryCert);
        label.setPadding(new Insets(3));
        resLabel.setPadding(new Insets(3));
        hb.setPadding(new Insets(20));
        hb.setSpacing(10);
        resBox.setSpacing(45);
        hb.setAlignment(Pos.TOP_CENTER);
        resBox.setAlignment(Pos.TOP_CENTER);
        imageBox.setAlignment(Pos.TOP_CENTER);
        resBox.getChildren().add(resLabel);
        resBox.getChildren().add(res);
        vb.getChildren().addAll(hb, resBox, imageBox);
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
            query(queryTF.getText());
        }
    }

    @FXML
    private void showAbout(ActionEvent event){
        List<CommandLinksDialog.CommandLinksButtonType> clb = Arrays.asList(
                new CommandLinksDialog.CommandLinksButtonType("https://github.com/wgpsec/fofa_viewer", "有问题请先查看说明", true),
                new CommandLinksDialog.CommandLinksButtonType("https://github.com/wgpsec/fofa_viewer/issues", "有bug请提交issue", true)
        );
        CommandLinksDialog dialog = new CommandLinksDialog(clb);
        dialog.setOnCloseRequest(e -> {
            ButtonType result = dialog.getResult();
            URI uri = URI.create(result.getText());
            Desktop dp = Desktop.getDesktop();
            if (dp.isSupported(Desktop.Action.BROWSE)) {
                try {
                    dp.browse(uri);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), e);
                }
            }
        });
        dialog.setTitle("提示");
        dialog.setContentText("WgpSec Team");
        dialog.showAndWait();
    }

    /**
     * 导出查询数据到excel文件
     */
    @FXML
    private void exportAction(ActionEvent e) {
        Tab tab = tabPane.getCurrentTab();
        if(tab.getText().equals("首页")) return;  // 首页无数据可导出
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择导出结果到目录...");
        File file = directoryChooser.showDialog(stage);
        if(file != null){
            TabDataBean bean = this.tabPane.getTabDataBean(tab);
            HashMap<String,String> urlList = new HashMap<>();
            List<List<String>> urls = new ArrayList<>();
            TableView<TableBean> tableView = (TableView<TableBean>) ((BorderPane) tab.getContent()).getCenter();
            HashMap<String, ExcelData> totalData = new HashMap<>();
            StringBuilder errorPage = new StringBuilder();

            if(bean.hasMoreData){ // 本地未完全加载时从网络请求进行加载
                int maxCount = Math.min(bean.total, client.max);
                int totalPage = (int)Math.ceil(maxCount/ Double.parseDouble(client.getSize()));
                TextInputDialog td = new TextInputDialog();
                td.setTitle("导出确认");
                td.setHeaderText("当前查询条件可查询到" + bean.total + "条数据，最多只能导出" + maxCount + "条数据，总共" + totalPage + "页\n全部导出请直接点击确认，选择导出部分数据请输入页数。");
                td.setContentText("请输入要导出的页数：");
                Optional<String> result = td.showAndWait();
                if (result.isPresent()){
                    if(!result.get().equals("")){
                        int inputPage = -1;
                        try{
                            inputPage = Integer.parseInt(result.get());
                            if(inputPage <= 0 || inputPage > totalPage){
                                showAlert(Alert.AlertType.ERROR, null, "输入的值必须介于0到"+totalPage+"之间！");
                                return;
                            }
                        }catch (NumberFormatException ex){
                            showAlert(Alert.AlertType.ERROR,null,"输入的值不是整数！");
                            return;
                        }
                        totalPage = inputPage;
                    }
                }else{
                    return;
                }
                int finalTotalPage = totalPage;
                Task<Void> exportTask = new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            for (int i = 1; i <= finalTotalPage; i++) {
                                Thread.sleep(500);
                                String text = replaceString(tab.getText());
                                HashMap<String, String> result = null;
                                result = helper.getHTML(client.getParam(String.valueOf(i)) + RequestHelper.encode(text), 50000, 50000);
                                if (result.get("code").equals("200")) {
                                    JSONObject obj = JSON.parseObject(result.get("msg"));
                                    loadJsonData(null, obj, totalData, urlList, true);
                                    updateMessage("已加载" + i + "/" + finalTotalPage + "页");
                                    updateProgress(i, finalTotalPage);
                                } else if (result.get("code").equals("error")) {
                                    // 请求失败时 等待0.5s再次请求
                                    Thread.sleep(500);
                                    result = helper.getHTML(client.getParam(String.valueOf(i)) + RequestHelper.encode(text), 50000, 50000);
                                    if (result.get("code").equals("error")) {
                                        errorPage.append(i).append(" ");
                                        continue;
                                    }
                                    JSONObject obj = JSON.parseObject(result.get("msg"));
                                    loadJsonData(null, obj, totalData, urlList, true);
                                    updateMessage("已加载" + i + "/" + finalTotalPage + "页");
                                    updateProgress(i, finalTotalPage);
                                }
                            }
                        }catch (InterruptedException e){
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                        return null;
                    }
                };
                ProgressDialog pd = new ProgressDialog(exportTask);
                pd.setTitle("正在导出数据中...");
                pd.setHeaderText("数据总量为" + maxCount + "，总共需要导出" + finalTotalPage + "页");
                new Thread(exportTask).start();
                pd.showAndWait();
            }else{ // 从本地数据加载，不再进行网络请求
                urlList.putAll(bean.dataList);
                for(TableBean i : tableView.getItems()){
                    ExcelData data = new ExcelData(
                            i.host.getValue(), i.title.getValue(), i.ip.getValue(), i.domain.getValue(),
                            i.port.getValue(), i.protocol.getValue(), i.server.getValue()
                    );
                    totalData.put(i.host.getValue(), data);
                }
            }
            for(String i : urlList.keySet()){
                List<String> item = new ArrayList<>();
                String t = urlList.get(i);
                if(!t.startsWith("http")){
                    item.add("http://" + t);
                }else{
                    item.add(t);
                }
                urls.add(item);
            }
            String fileName = file.getAbsolutePath() + System.getProperty("file.separator") + "fofa导出结果" + System.currentTimeMillis() + ".xlsx";
            ExcelWriter excelWriter = null;
            try {
                excelWriter = EasyExcel.write(fileName).build();
                OnceAbsoluteMergeStrategy strategy = new OnceAbsoluteMergeStrategy(0,1,0,6);
                ArrayList<ArrayList<String>> head = new ArrayList<ArrayList<String>>(){{add(new ArrayList<String>(){{add(tab.getText());}});}};
                WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
                WriteFont contentWriteFont = new WriteFont();
                contentWriteFont.setFontHeightInPoints((short)16);
                contentWriteCellStyle.setWriteFont(contentWriteFont);
                WriteSheet writeSheet0 = EasyExcel.writerSheet(0,"查询title")
                        .registerWriteHandler(strategy).registerWriteHandler(new HorizontalCellStyleStrategy(null, contentWriteCellStyle)).build();
                excelWriter.write(head, writeSheet0);
                WriteSheet writeSheet1 = EasyExcel.writerSheet(1, "查询结果").head(ExcelData.class).build();
                excelWriter.write(new ArrayList<>(totalData.values()), writeSheet1);
                WriteSheet writeSheet2 = EasyExcel.writerSheet(2, "urls").build();
                excelWriter.write(urls, writeSheet2);
                if(errorPage.length() == 0){
                    showAlert(Alert.AlertType.INFORMATION, null, "导出成功！文件保存在" + fileName);
                }else{
                    showAlert(Alert.AlertType.INFORMATION, null, "部分数据导出成功，其中第"+ errorPage.toString() +"页加载失败，文件保存在" + fileName);
                }
            }catch(Exception exception){
                logger.log(Level.WARNING, exception.getMessage(), e);
                showAlert(Alert.AlertType.INFORMATION, null, "导出失败！");
            }finally {
                if (excelWriter != null) {
                    excelWriter.finish();
                }
            }
        }
    }

    /**
     * 处理查询结果
     */
    public void query(String text){
        String tabTitle = text.trim();
        if(text.startsWith("(*)")){
            tabTitle = text;
            text = text.substring(3);
            text = "(" + text + ") && (is_honeypot=false && is_fraud=false)";
        }
        if(checkHoneyPot.isSelected() && !text.contains("(is_honeypot=false && is_fraud=false)")){
            tabTitle = "(*)" + text;
            text = "(" + text + ") && (is_honeypot=false && is_fraud=false)";
        }
        if(this.tabPane.isExistTab(tabTitle)){ // 若已存在同名Tab 则直接跳转，不查询
            this.tabPane.setCurrentTab(this.tabPane.getTab(text));
            return;
        }
        HashMap<String,String> result = helper.getHTML(client.getParam(null) + RequestHelper.encode(text), 50000, 50000);
        if(result.get("code").equals("200")){
            Tab tab = new Tab();
            TabDataBean _tmp = new TabDataBean();
            tab.setText(tabTitle);
            JSONObject obj = JSON.parseObject(result.get("msg"));
            if(obj.getBoolean("error")){
                showAlert(Alert.AlertType.ERROR, null, obj.getString("errmsg"));
                return;
            }
            if(obj.getInteger("size") < Integer.parseInt(client.getSize())){
                _tmp.hasMoreData = false;
            }
            this.tabPane.addTab(tab, _tmp);
            StatusBar bar = new StatusBar();
            this.tabPane.addBar(tab, bar);
            _tmp.total = obj.getInteger("size");
            Label totalLabel = new Label("当前查询条件查询到 " + obj.getString("size"));
            Label loadedLabel = new Label(" 条，当前已加载 ");
            Label countLabel = new Label(String.valueOf(obj.getJSONArray("results").size()));
            Label tmpLabel = new Label(" 条");
            bar.getRightItems().addAll(new ArrayList<Label>(){{ add(totalLabel); add(loadedLabel); add(countLabel); add(tmpLabel);}});
            decoratedField.addLog(tabTitle);
            BorderPane tablePane = new BorderPane();
            Map<String, TableBean> values = (Map<String, TableBean>) loadJsonData(_tmp, obj, null, null, false);
            TableView<TableBean> view = new TableView<>(FXCollections.observableArrayList(values.values()));
            setTable(view);
            tablePane.setCenter(view);
            tablePane.setBottom(bar);
            tab.setContent(tablePane);
            tabPane.setCurrentTab(tab);
            // 设置延时任务在滚动条界面渲染结束后进行事件绑定
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(event -> addScrollBarListener(view));
            pause.playFromStart();
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
            client = new FofaBean(properties.getProperty("email").trim(), properties.getProperty("key").trim());
            client.setSize(properties.getProperty("maxSize"));
        } catch (IOException e){
            showAlert(Alert.AlertType.ERROR, null, "缺少配置文件config.properties！");
            Platform.exit();  //结束进程
        }
    }

    /**
     * 表格配置
     * @param view
     */
    public void setTable(TableView<TableBean> view){
        TableColumn<TableBean, Integer> num = new TableColumn<>("序号");
        TableColumn<TableBean, String> host = new TableColumn<>("HOST");
        TableColumn<TableBean, String> title = new TableColumn<>("标题");
        TableColumn<TableBean, String> ip = new TableColumn<>("IP");
        TableColumn<TableBean, Integer> port = new TableColumn<>("端口");
        TableColumn<TableBean, String> domain = new TableColumn<>("域名");
        TableColumn<TableBean, String> protocol = new TableColumn<>("协议");
        TableColumn<TableBean, String> server = new TableColumn<>("Server指纹");
        num.setCellValueFactory(param -> param.getValue().getNum().asObject());
        host.setCellValueFactory(param -> param.getValue().getHost());
        title.setCellValueFactory(param -> param.getValue().getTitle());
        ip.setCellValueFactory(param -> param.getValue().getIp());
        port.setCellValueFactory(param -> param.getValue().getPort().asObject());
        domain.setCellValueFactory(param -> param.getValue().getDomain());
        protocol.setCellValueFactory(param -> param.getValue().getProtocol());
        server.setCellValueFactory(param -> param.getValue().getServer());
        view.getColumns().add(num);
        view.getColumns().addAll(new ArrayList<TableColumn<TableBean,String>>(){{ add(host);add(title);add(ip);}});
        view.getColumns().add(port);
        view.getColumns().addAll(new ArrayList<TableColumn<TableBean,String>>(){{add(domain);add(protocol);add(server);}});
        view.setRowFactory(param -> {
            final TableRow<TableBean> row = new TableRow<>();
            // 设置表格右键菜单
            ContextMenu rowMenu = new ContextMenu();
            MenuItem copyLink = new MenuItem("复制链接");
            copyLink.setOnAction(event -> { //设置剪贴板
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(row.getItem().host.getValue());
                clipboard.setContent(content);
            });
            MenuItem queryCSet = new MenuItem("查询对应C段资产");
            queryCSet.setOnAction(event -> {
                String ip1 = row.getItem().ip.getValue();
                String queryText = ip1.substring(0, ip1.lastIndexOf('.'));
                query("ip=\""+ queryText + ".0/24" + "\"");
            });
            MenuItem querySubdomain = new MenuItem("查询相关域名资产");
            querySubdomain.setOnAction(event -> {
                String domain1 = row.getItem().domain.getValue();
                if(!domain1.equals("")){
                    query("domain=\""+ domain1 + "\"");
                }
            });
            MenuItem favicon = new MenuItem("从fofa搜索favicon相关的资产");
            favicon.setOnAction(event -> {
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
            });
            MenuItem cert = new MenuItem("从fofa搜索证书相关的资产");
            cert.setOnAction(event -> {
                String host1 = row.getItem().host.getValue();
                if(host1.startsWith("https")){
                    try {
                        String value = helper.getCertSerialNum(host1);
                        if(value != null){
                            query(value);
                        }
                    }catch (Exception e){
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }else{
                    showAlert(Alert.AlertType.WARNING, null, "请选中host中带有https的行再点击查询证书相关资产");
                }
            });
            rowMenu.getItems().addAll(copyLink, queryCSet, querySubdomain, favicon, cert);
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
            // 双击行时使用默认浏览器打开
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    String url = row.getItem().host.getValue();
                    String protocol1 = row.getItem().protocol.getValue();
                    String domain1 = row.getItem().domain.getValue();
                    if(!domain1.equals("") || url.startsWith("http") || protocol1.equals("") || protocol1.startsWith("http")){
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
                                logger.log(Level.WARNING, e.getMessage(), e);
                            }
                        }
                    }
                }
            });
            return row;
        });
        view.getSortOrder().add(num);
    }

    /**
     * 设置滚动自动加载
     * @param view tabview
     */
    private void addScrollBarListener(TableView<?> view){
        ScrollBar bar = null;
        for(Node n : view.lookupAll(".scroll-bar")) {
            if(n instanceof ScrollBar) {
                ScrollBar _bar = (ScrollBar) n;
                if(_bar.getOrientation().equals(Orientation.VERTICAL)) {
                    bar = _bar;
                }
            }
        }
        assert bar != null;
        bar.valueProperty().addListener((observable, oldValue, newValue) -> {
            if((double)newValue == 1.0D){
                Tab tab = tabPane.getCurrentTab();
                TabDataBean bean = tabPane.getTabDataBean(tab);
                if(bean.hasMoreData){
                    bean.page += 1;
                    TableView<TableBean> tableView = (TableView<TableBean>) ((BorderPane) tab.getContent()).getCenter();
                    String text = replaceString(tab.getText());
                    HashMap<String,String> result = helper.getHTML(client.getParam(String.valueOf(bean.page)) + RequestHelper.encode(text), 20000, 20000);
                    if(result.get("code").equals("200")){
                        JSONObject obj = JSON.parseObject(result.get("msg"));
                        if(obj.getBoolean("error")){
                            return;
                        }
                        Map<String, TableBean> list = (Map<String, TableBean>) loadJsonData(bean, obj, null, null, false);
                        if (list.keySet().size() !=0){
                            ObservableList<TableBean> _tmp = tableView.getItems();
                            TableBean b = _tmp.get(_tmp.size()-5);
                            List<TableBean> tmp = list.values().stream().sorted(Comparator.comparing(TableBean::getIntNum)).collect(Collectors.toList());
                            tableView.getItems().addAll(FXCollections.observableArrayList(tmp));
                            tableView.scrollTo(b);
                            StatusBar statusBar = this.tabPane.getBar(tab);
                            Label countLabel = (Label) statusBar.getRightItems().get(2);
                            countLabel.setText(String.valueOf(Integer.parseInt(countLabel.getText()) + obj.getJSONArray("results").size()));
                        }
                        if(bean.page * Integer.parseInt(client.getSize()) > obj.getInteger("size")){
                            bean.hasMoreData = false;
                        }
                    }
                }
            }
        });
    }

    private Map<String, ? extends BaseBean> loadJsonData(TabDataBean bean,
                                                         JSONObject obj, HashMap<String, ExcelData> excelData,
                                                         HashMap<String,String> urlList, boolean isExport){
        JSONArray array = obj.getJSONArray("results");
        HashMap<String, TableBean> list = new HashMap<>();
        for(int index=0; index < array.size(); index ++){
            JSONArray _array = array.getJSONArray(index);
            String host = _array.getString(0);
            String title = _array.getString(1);
            String ip = _array.getString(2);
            String domain = _array.getString(3);
            int port = Integer.parseInt(_array.getString(4));
            String protocol = _array.getString(5);
            String server = _array.getString(6);
            String _host = ip+":"+port;

            if(isExport){ // 导出数据
                ExcelData d = new ExcelData(host, title, ip, domain, port, protocol, server);
                getUrlList(urlList, host, ip, port, protocol, _host);
                //去除http 的重复项
                if(excelData.containsKey(_host) && protocol.equals("")){
                    excelData.remove(_host);
                }
                // 去除80端口的重复项
                if(port==80 && excelData.containsKey(_host)){
                    excelData.remove(_host);
                }
                // 去除 443 和 https 的重复项
                if((excelData.containsKey(host) || excelData.containsKey("https://"+host)) && protocol.startsWith("http")){
                    continue;
                }else if(excelData.containsKey(ip) && protocol.equals("http")){
                    continue;
                }
                excelData.put(host, d);

            }else{  // table 页更新数据
                TableBean b = new TableBean(0, host, title, ip, domain, port, protocol, server);
                getUrlList(bean.dataList, host, ip, port, protocol, _host);
                //去除http 的重复项
                if(list.containsKey(_host) && protocol.equals("")){
                    b.num = list.get(_host).num;
                    list.remove(_host);
                }
                // 去除80端口的重复项
                if(port==80 && list.containsKey(_host)){
                    b.num = list.get(_host).num;
                    list.remove(_host);
                }
                // 去除 443 和 https 的重复项
                if((list.containsKey(host) || list.containsKey("https://"+host)) && protocol.startsWith("http")){
                    continue;
                }else if(list.containsKey(ip) && protocol.equals("http")){
                    continue;
                }
                if(b.num.getValue() == 0){ b.num.set(++bean.count);}
                list.put(host, b);
            }
        }
        return list;
    }

    private void getUrlList(HashMap<String, String> urlList, String host, String ip, int port, String protocol, String _host) {
        if (protocol.equals("")) {
            urlList.put(host, host);
        }else if(port == 80 && protocol.equals("http")){
            urlList.put("http://" + ip, "http://" + ip);
        } else if (protocol.equals("http") && !urlList.containsKey(_host)) {
            urlList.put("http://" + host, "http://" + host);
        } else if (port == 443 && protocol.equals("https") && !urlList.containsKey("https://" + ip)) {
            urlList.put("https://" + ip, "https://" + ip);
        } else if (port != 443 && protocol.equals("https") && !urlList.containsKey("https://" + _host)) {
            urlList.put("https://" + _host, "https://" + _host);
        }
    }

    /**
     * 对话框配置
     * @param type 对话框类型
     * @param header 对话框标题
     * @param content 对话框内容
     */
    public void showAlert(Alert.AlertType type, String header, String content){
        Alert alert = new Alert(type);
        alert.setTitle("提示");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public String replaceString(String tabTitle){
        if(tabTitle.startsWith("(*)")){
            tabTitle = tabTitle.substring(3);
            tabTitle = "(" + tabTitle + ") && (is_honeypot=false && is_fraud=false)";
        }
        return tabTitle;
    }
}
