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
import org.fofaviewer.controls.RequestTask;
import org.fofaviewer.utils.CertRequestUtil;
import org.fofaviewer.utils.LogUtil;
import org.fofaviewer.utils.RequestHelper;
import org.fofaviewer.controls.CloseableTabPane;
import org.controlsfx.control.textfield.TextFields;
import org.fofaviewer.utils.ResourceBundleUtil;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * 处理事件
 */
public class MainController {
    @FXML
    private MenuItem help;
    @FXML
    private MenuItem about;
    @FXML
    private Button exportDataBtn;
    @FXML
    private Button searchBtn;
    @FXML
    private Label queryString;
    @FXML
    private VBox rootLayout;
    @FXML
    private TextField queryTF;
    @FXML
    private CheckBox checkHoneyPot;
    @FXML
    private CheckBox withFid;
    @FXML
    private CloseableTabPane tabPane;
    private AutoHintTextField decoratedField;
    private static final RequestHelper helper = RequestHelper.getInstance();
    private static FofaBean client;
    private final ResourceBundle resourceBundle;

    public MainController(){
        this.resourceBundle = ResourceBundleUtil.getResource();
    }
    /**
     * 初始化
     */
    @FXML
    private void initialize() {
        //switch language
        about.setText(resourceBundle.getString("ABOUT"));
        help.setText(resourceBundle.getString("HELP"));
        searchBtn.setText(resourceBundle.getString("SEARCH"));
        exportDataBtn.setText(resourceBundle.getString("EXPORT_BUTTON"));
        queryString.setText(resourceBundle.getString("QUERY_CONTENT"));
        checkHoneyPot.setText(resourceBundle.getString("REMOVE_HONEYPOTS"));
        withFid.setText(resourceBundle.getString("WITH_FID"));
        decoratedField = new AutoHintTextField(queryTF);
        loadConfigure();
        //初始化起始页tab
        Tab tab = this.tabPane.getTab(resourceBundle.getString("HOMEPAGE"));
        Button queryCert = new Button(resourceBundle.getString("QUERY_BUTTON"));
        Button queryFavicon = new Button(resourceBundle.getString(("QUERY_BUTTON")));
        Label label = new Label(resourceBundle.getString("CERT_LABEL"));
        Label faviconLabel = new Label(resourceBundle.getString("FAVICON_LABEL"));
        TextField tf = TextFields.createClearableTextField();
        TextField favionTF = TextFields.createClearableTextField();
        Image image = new Image(Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()) ? "api_doc_cn.png" : "api_doc_en.png");
//        Image image = new Image("api_doc_en.png");
        ImageView view = new ImageView(image);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(view);
        view.setFitHeight(1600D);
        view.setFitWidth(750D);
        scrollPane.setPrefWidth(760);
        tf.setPromptText(resourceBundle.getString("CERT_HINT"));
        favionTF.setPromptText(resourceBundle.getString("FAVICON_HINT"));
        tf.setPrefWidth(400);
        favionTF.setPrefWidth(400);
        label.setFont(Font.font(14));
        faviconLabel.setFont(Font.font(14));
        queryCert.setOnAction(event -> {
            String txt = tf.getText().trim();
            if(!txt.isEmpty()){
                String serialnumber = txt.replaceAll(" ", "");
                BigInteger i = new BigInteger(serialnumber, 16);
                query("cert=\"" + i.toString() + "\"");
            }
        });
        queryFavicon.setOnAction(event -> {
            String url = favionTF.getText().trim();
            if(!url.isEmpty()){
                if(!url.startsWith("http")){
                    showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("ERROR_URL"));
                }else {
                    HashMap<String,String> res = helper.getImageFavicon(url);
                    if(res != null){
                        if(res.get("code").equals("error")){
                            showAlert(Alert.AlertType.ERROR, null, res.get("msg"));return;
                        }
                        query(res.get("msg"));
                    }
                }
            }
        });
        VBox vb = new VBox();
        HBox hb = new HBox();
        HBox faviconBox = new HBox();
        HBox imageBox = new HBox();
        vb.setSpacing(10);
        imageBox.getChildren().add(scrollPane);
        hb.getChildren().addAll(label, tf, queryCert);
        label.setPadding(new Insets(3));
        faviconLabel.setPadding(new Insets(3,5,3,3));
        hb.setPadding(new Insets(10));
        hb.setSpacing(15);
        faviconBox.setSpacing(15); // 设置控件间距
        hb.setAlignment(Pos.TOP_CENTER);
        faviconBox.setAlignment(Pos.TOP_CENTER);
        faviconBox.setPadding(new Insets(5,0,5,0));
        imageBox.setAlignment(Pos.TOP_CENTER);
        imageBox.setPadding(new Insets(5,0,10,0));
        faviconBox.getChildren().addAll(faviconLabel, favionTF, queryFavicon);
        vb.getChildren().addAll(hb, faviconBox, imageBox);
        //VBox.setMargin(res, new Insets(0, 100, 0 ,100));
        tab.setContent(vb);
    }

    /**
     * 查询按钮点击事件，与fxml中命名绑定
     * @param event 点击事件
     */
    @FXML
    private void queryAction(ActionEvent event){
        if(queryTF.getText() != null){
            searchBtn.setDisable(true);
            query(queryTF.getText());
        }
    }

    @FXML
    private void showAbout(ActionEvent event){
        List<CommandLinksDialog.CommandLinksButtonType> clb = Arrays.asList(
                new CommandLinksDialog.CommandLinksButtonType("https://github.com/wgpsec/fofa_viewer",
                        resourceBundle.getString("ABOUT_HINT1"), true),
                new CommandLinksDialog.CommandLinksButtonType("https://github.com/wgpsec/fofa_viewer/issues",
                        resourceBundle.getString("ABOUT_HINT2"), true)
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
                    LogUtil.log("Controller", ex, Level.WARNING);
                }
            }
        });
        dialog.setTitle("Notice");
        dialog.setContentText("WgpSec Team");
        dialog.showAndWait();
    }

    /**
     * 导出查询数据到excel文件
     */
    @FXML
    private void exportAction(ActionEvent e) {
        Tab tab = tabPane.getCurrentTab();
        if(tab.getText().equals(resourceBundle.getString("HOMEPAGE"))) return;  // 首页无数据可导出
        Stage stage = (Stage) rootLayout.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(resourceBundle.getString("DIRECTORY_CHOOSER_TITLE"));
        File file = directoryChooser.showDialog(stage);
        if(file != null){
            TabDataBean bean = this.tabPane.getTabDataBean(tab);
            HashMap<String,String> urlList = new HashMap<>();
            List<List<String>> urls = new ArrayList<>();
            TableView<TableBean> tableView = (TableView<TableBean>) ((BorderPane) tab.getContent()).getCenter();
            HashMap<String, ExcelBean> totalData = new HashMap<>();
            StringBuilder errorPage = new StringBuilder();

            if(bean.hasMoreData){ // 本地未完全加载时从网络请求进行加载
                int maxCount = Math.min(bean.total, client.max);
                int totalPage = (int)Math.ceil(maxCount/ Double.parseDouble(client.getSize()));
                TextInputDialog td = new TextInputDialog();
                td.setTitle(resourceBundle.getString("EXPORT_CONFIRM"));
                td.setHeaderText(resourceBundle.getString("EXPORT_HINT1") + bean.total + resourceBundle.getString("EXPORT_HINT2")
                        + maxCount + resourceBundle.getString("EXPORT_HINT3") + totalPage + resourceBundle.getString("EXPORT_HINT4"));
                td.setContentText(resourceBundle.getString("EXPORT_CONTENT"));
                Optional<String> result = td.showAndWait();
                if (result.isPresent()){
                    if(!result.get().isEmpty()){
                        int inputPage = -1;
                        try{
                            inputPage = Integer.parseInt(result.get());
                            if(inputPage <= 0 || inputPage > totalPage){
                                showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("EXPORT_INPUT_NUM_HINT1")
                                        + totalPage + resourceBundle.getString("EXPORT_INPUT_NUM_HINT2"));
                                return;
                            }
                        }catch (NumberFormatException ex){
                            showAlert(Alert.AlertType.ERROR,null, resourceBundle.getString("EXPORT_INPUT_NUM_ERROR"));
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
                                HashMap<String, String> result = helper.getHTML(client.getParam(String.valueOf(i), withFid.isSelected())
                                        + helper.encode(text), 50000, 50000);
                                if (result.get("code").equals("200")) {
                                    JSONObject obj = JSON.parseObject(result.get("msg"));
                                    loadJsonData(null, obj, totalData, urlList, true, null);
                                    updateMessage(resourceBundle.getString("LOADDATA_HINT1") + i + "/"
                                            + finalTotalPage + resourceBundle.getString("LOADDATA_HINT2"));
                                    updateProgress(i, finalTotalPage);
                                } else if (result.get("code").equals("error")) {
                                    // 请求失败时 等待1s再次请求
                                    Thread.sleep(1000);
                                    result = helper.getHTML(client.getParam(String.valueOf(i), withFid.isSelected())
                                            + helper.encode(text), 50000, 50000);
                                    if (result.get("code").equals("error")) {
                                        errorPage.append(i).append(" ");
                                        continue;
                                    }
                                    JSONObject obj = JSON.parseObject(result.get("msg"));
                                    loadJsonData(null, obj, totalData, urlList, true, null);
                                    updateMessage(resourceBundle.getString("LOADDATA_HINT1") + i + "/" + finalTotalPage
                                            + resourceBundle.getString("LOADDATA_HINT2"));
                                    updateProgress(i, finalTotalPage);
                                }
                            }
                        }catch (InterruptedException e){
                            LogUtil.log("Controller", e, Level.WARNING);
                        }
                        return null;
                    }
                };
                ProgressDialog pd = new ProgressDialog(exportTask);
                pd.setTitle(resourceBundle.getString("EXPORT_TITLE"));
                pd.setHeaderText(resourceBundle.getString("EXPORT_HEADER1") + maxCount
                        + resourceBundle.getString("EXPORT_HEADER2")
                        + finalTotalPage + resourceBundle.getString("EXPORT_HEADER3"));
                new Thread(exportTask).start();
                pd.showAndWait();
            }else{ // 从本地数据加载，不再进行网络请求
                urlList.putAll(bean.dataList);
                for(TableBean i : tableView.getItems()){
                    ExcelBean data = new ExcelBean(
                            i.host.getValue(), i.title.getValue(), i.ip.getValue(), i.domain.getValue(),
                            i.port.getValue(), i.protocol.getValue(), i.server.getValue(), i.fid.getValue(), i.certCN.getValue()
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
            String fileName = file.getAbsolutePath() + System.getProperty("file.separator")
                    + resourceBundle.getString("EXPORT_FILENAME") + System.currentTimeMillis() + ".xlsx";
            ExcelWriter excelWriter = null;
            try {
                excelWriter = EasyExcel.write(fileName).build();
                OnceAbsoluteMergeStrategy strategy = new OnceAbsoluteMergeStrategy(0,1,0,6);
                ArrayList<ArrayList<String>> head = new ArrayList<ArrayList<String>>(){{add(new ArrayList<String>(){{add(tab.getText());}});}};
                WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
                WriteFont contentWriteFont = new WriteFont();
                contentWriteFont.setFontHeightInPoints((short)16);
                contentWriteCellStyle.setWriteFont(contentWriteFont);
                WriteSheet writeSheet0 = EasyExcel.writerSheet(0, resourceBundle.getString("EXPORT_FILENAME_SHEET1"))
                        .registerWriteHandler(strategy)
                        .registerWriteHandler(new HorizontalCellStyleStrategy(null, contentWriteCellStyle)).build();
                excelWriter.write(head, writeSheet0);
                WriteSheet writeSheet1 = EasyExcel.writerSheet(1, resourceBundle.getString("EXPORT_FILENAME_SHEET2"))
                        .head(ExcelBean.class).build();
                excelWriter.write(new ArrayList<>(totalData.values()), writeSheet1);
                WriteSheet writeSheet2 = EasyExcel.writerSheet(2, resourceBundle.getString("EXPORT_FILENAME_SHEET3")).build();
                excelWriter.write(urls, writeSheet2);
                if(errorPage.length() == 0){
                    showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_MESSAGE1") + fileName);
                }else{
                    showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_MESSAGE2_1")
                            + errorPage.toString() + resourceBundle.getString("EXPORT_MESSAGE2_2") + " " + fileName);
                }
            }catch(Exception exception){
                LogUtil.log("Controller", exception, Level.WARNING);
                showAlert(Alert.AlertType.INFORMATION, null, resourceBundle.getString("EXPORT_ERROR"));
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
        RequestTask task = new RequestTask(client.getParam(null, withFid.isSelected()) + helper.encode(text), tabTitle);
        task.valueProperty().addListener((observable, oldValue, newValue) -> {
            HashMap<String, String> result = task.getValue();
            if (result != null) {
                if (result.get("code").equals("200")) {
                    Tab tab = new Tab();
                    TabDataBean _tmp = new TabDataBean();
                    tab.setText(task.getTabTitle());
                    tab.setTooltip(new Tooltip(tab.getText()));
                    JSONObject obj = JSON.parseObject(result.get("msg"));
                    if (obj.getBoolean("error")) {
                        showAlert(Alert.AlertType.ERROR, null, obj.getString("errmsg"));
                        return;
                    }
                    if (obj.getInteger("size") < Integer.parseInt(client.getSize())) {
                        _tmp.hasMoreData = false;
                    }
                    tabPane.addTab(tab, _tmp);
                    tabPane.setCurrentTab(tab);
                    StatusBar bar = new StatusBar();
                    tabPane.addBar(tab, bar);
                    _tmp.total = obj.getInteger("size");
                    Label totalLabel = new Label(resourceBundle.getString("QUERY_TIPS1") + obj.getString("size"));
                    Label countLabel = new Label(String.valueOf(obj.getJSONArray("results").size()));
                    bar.getRightItems().addAll(new ArrayList<Label>() {{
                        add(totalLabel);
                        add(new Label(resourceBundle.getString("QUERY_TIPS2")));
                        add(countLabel);
                        add(new Label(resourceBundle.getString("QUERY_TIPS3")));
                    }});
                    decoratedField.addLog(task.getTabTitle());
                    BorderPane tablePane = new BorderPane();
                    TableView<TableBean> view = new TableView<>();
                    Map<String, TableBean> values = (Map<String, TableBean>) loadJsonData(_tmp, obj, null, null, false, view);
                    view.setItems(FXCollections.observableArrayList(values.values()));
                    setTable(view);
                    tablePane.setCenter(view);
                    tablePane.setBottom(bar);
                    tab.setContent(tablePane);
                    // 设置延时任务在滚动条界面渲染结束后进行事件绑定
                    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                    pause.setOnFinished(event -> addScrollBarListener(view));
                    pause.playFromStart();
                } else {
                    showAlert(Alert.AlertType.ERROR, result.get("code"), result.get("msg"));
                }
            }
            searchBtn.setDisable(false);
        });
        new Thread(task).start();
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
            showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("LOAD_CONFIG_ERROR"));
            Platform.exit();  //结束进程
        }
    }

    /**
     * 表格配置
     */
    public void setTable(TableView<TableBean> view){
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
        if(!withFid.isSelected()){ // 未勾选fid时默认不显示
            fid.setVisible(false);
        }
        // 修改ip的排序规则
        ip.setComparator(Comparator.comparing(MainController::getValueFromIP));
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
                query("ip=\"" + _ip + "/32" + "\"");
            });
            MenuItem queryCSet = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_C-CLASS"));
            queryCSet.setOnAction(event -> {
                String _ip = row.getItem().ip.getValue();
                query("ip=\""+ _ip.substring(0, _ip.lastIndexOf('.')) + ".0/24" + "\"");
            });
            MenuItem querySubdomain = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_QUERY_DOAMIN"));
            querySubdomain.setOnAction(event -> {
                String domain1 = row.getItem().domain.getValue();
                if(!domain1.isEmpty()){
                    query("domain=\""+ domain1 + "\"");
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
                        showAlert(Alert.AlertType.ERROR, null, res.get("msg"));return;
                    }
                    query(res.get("msg"));
                    return;
                }
                showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("QUERY_FAVICON_ERROR"));
            });
            MenuItem queryCert = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_CERT"));
            queryCert.setOnAction(event -> {
                String sn = row.getItem().cert.getValue();
                if(sn.isEmpty()){
                    showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("QUERY_CERT_ERROR"));
                }else{
                    query("cert=" + sn);
                }
            });
            MenuItem fidMenu = new MenuItem(resourceBundle.getString("TABLE_CONTEXTMENU_FID"));
            fidMenu.setOnAction(event -> {
                String _fid = row.getItem().fid.getValue();
                if(!_fid.isEmpty()){
                    query("fid=\""+_fid+"\"");
                }else{
                    showAlert(Alert.AlertType.WARNING, null,resourceBundle.getString("QUERY_FID_ERROR"));
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
                                LogUtil.log("Controller", e, Level.WARNING);
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
                    RequestTask task = new RequestTask(client.getParam(String.valueOf(bean.page), withFid.isSelected())
                            + helper.encode(text), null);
                    task.valueProperty().addListener((observable1, oldValue1, newValue1) -> {
                        HashMap<String,String> result = task.getValue();
                        if(result != null) {
                            if (result.get("code").equals("200")) {
                                JSONObject obj = JSON.parseObject(result.get("msg"));
                                if (obj.getBoolean("error")) {
                                    return;
                                }
                                Map<String, TableBean> list = (Map<String, TableBean>) loadJsonData(bean, obj, null, null, false, tableView);
                                if (list.keySet().size() != 0) {
                                    ObservableList<TableBean> _tmp = tableView.getItems();
                                    TableBean b = _tmp.get(_tmp.size() - 5);
                                    List<TableBean> tmp = list.values().stream().sorted(Comparator.comparing(TableBean::getIntNum)).collect(Collectors.toList());
                                    tableView.getItems().addAll(FXCollections.observableArrayList(tmp));
                                    tableView.scrollTo(b);
                                    StatusBar statusBar = tabPane.getBar(tab);
                                    Label countLabel = (Label) statusBar.getRightItems().get(2);
                                    countLabel.setText(String.valueOf(Integer.parseInt(countLabel.getText()) + obj.getJSONArray("results").size()));
                                }
                                if (bean.page * Integer.parseInt(client.getSize()) > obj.getInteger("size")) {
                                    bean.hasMoreData = false;
                                }
                            }
                        }
                    });
                    new Thread(task).start();
                }
            }
        });
    }

    public static Map<String, ? extends BaseBean> loadJsonData(TabDataBean bean,
                                                         JSONObject obj, HashMap<String, ExcelBean> excelData,
                                                         HashMap<String,String> urlList,
                                                               boolean isExport,
                                                               TableView<TableBean> view){
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
            String cert = _array.getString(7);
            String certCN = "";
            String fid;
            try{
                fid =  _array.getString(8);
            }catch(IndexOutOfBoundsException e){
                fid = "";
            }
            if(!cert.isEmpty()){
                certCN = helper.getCertSubjectDomainByFoFa(cert);
                cert = helper.getCertSerialNumberByFoFa(cert);
            }
            String _host = ip + ":" + port;

            if(isExport){ // 是否为导出数据
                ExcelBean d = new ExcelBean(host, title, ip, domain, port, protocol, server, fid, certCN);
                //去除http 的重复项
                if(excelData.containsKey(_host) && protocol.isEmpty()){
                    excelData.remove(_host);
                }
                // 去除80端口的重复项
                if(port==80 && excelData.containsKey(_host)){
                    excelData.remove(_host);
                }
                // 去除 443 和 https 的重复项
                if((excelData.containsKey(host) || excelData.containsKey("https://" + host)) && protocol.startsWith("http")){
                    continue;
                }else if(excelData.containsKey(ip) && protocol.equals("http")){
                    continue;
                }
                getUrlList(urlList, host, ip, port, protocol, _host);
                excelData.put(host, d);

            }else{  // table 页更新数据
                TableBean b = new TableBean(0, host, title, ip, domain, port, protocol, server, fid, cert, certCN);
                //去除http 的重复项
                if(list.containsKey(_host) && protocol.isEmpty()){
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
                getUrlList(bean.dataList, host, ip, port, protocol, _host);
                list.put(host, b);
            }
        }
        try {
            CertRequestUtil util = new CertRequestUtil(view);
            if (isExport) {
                util.getCertDomain(excelData, true);
            } else {
                util.getCertDomain(list, false);
            }
        }catch (InterruptedException e){
            LogUtil.log("Controller", e, Level.WARNING);
        }
        return list;
    }

    public static void getUrlList(HashMap<String, String> urlList, String host, String ip, int port, String protocol, String _host) {
        if (protocol.isEmpty() && host.endsWith("443") && !host.startsWith("http")){
            urlList.put("https://" + host, "https://" + host);
        }else if (protocol.isEmpty()) {
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
     * @param type dialog type
     * @param header dialog title
     * @param content content of dialog
     */
    private void showAlert(Alert.AlertType type, String header, String content){
        Alert alert = new Alert(type);
        alert.setTitle("提示");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String replaceString(String tabTitle){
        if(tabTitle.startsWith("(*)")){
            tabTitle = tabTitle.substring(3);
            tabTitle = "(" + tabTitle + ") && (is_honeypot=false && is_fraud=false)";
        }
        return tabTitle;
    }

    /**
     * 将IP地址转换为浮点数
     * @param ip IP
     * @return double value
     */
    private static Double getValueFromIP(String ip){
        String[] str = ip.split("\\.");
        return Double.parseDouble(str[0]) * 1000000 + Double.parseDouble(str[1]) * 1000
                + Double.parseDouble(str[2]) + Double.parseDouble(str[3]) * 0.001;
    }
}
