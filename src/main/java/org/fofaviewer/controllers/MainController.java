package org.fofaviewer.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import org.controlsfx.dialog.CommandLinksDialog;
import org.controlsfx.dialog.ProgressDialog;
import org.fofaviewer.bean.*;
import org.fofaviewer.controls.*;
import org.fofaviewer.main.FofaConfig;
import org.fofaviewer.request.MainControllerCallback;
import org.fofaviewer.request.Request;
import org.fofaviewer.request.RequestCallback;
import org.fofaviewer.utils.DataUtil;
import org.fofaviewer.utils.RequestUtil;
import org.controlsfx.control.textfield.TextFields;
import org.fofaviewer.utils.ResourceBundleUtil;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.fofaviewer.utils.SQLiteUtils;
import org.tinylog.Logger;

public class MainController {
    private boolean isProject;
    @FXML
    private Menu help;
    @FXML
    private Menu project;
    @FXML
    private Menu rule;
    @FXML
    private Menu config;
    @FXML
    private MenuItem createRule;
    @FXML
    private MenuItem exportRule;
    @FXML
    private MenuItem about;
    @FXML
    private MenuItem setConfig;
    @FXML
    private MenuItem openProject;
    @FXML
    private MenuItem saveProject;
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
    private CheckBox isAll;
    @FXML
    private CheckBox title;
    @FXML
    private CheckBox cert;
    @FXML
    private CloseableTabPane tabPane;
    private AutoHintTextField decoratedField;
    private static final RequestUtil helper = RequestUtil.getInstance();
    private static FofaConfig client;
    private final ResourceBundle resourceBundle;
    private final HashMap<CheckBox, String> keyMap = new HashMap<>();

    public MainController(){
        this.resourceBundle = ResourceBundleUtil.getResource();
    }
    /**
     * 初始化
     */
    @FXML
    private void initialize() {
        SQLiteUtils.init();
        keyMap.put(withFid, "fid");
        keyMap.put(cert, "cert");
        keyMap.put(title, "title");
        isProject = false;
        title.setText(resourceBundle.getString("TITLE"));
        cert.setText(resourceBundle.getString("CERT"));
        about.setText(resourceBundle.getString("ABOUT"));
        help.setText(resourceBundle.getString("HELP"));
        project.setText(resourceBundle.getString("PROJECT"));
        config.setText(resourceBundle.getString("CONFIG_PANEL"));
        rule.setText(resourceBundle.getString("RULE"));
        setConfig.setText(resourceBundle.getString("SET_CONFIG"));
        createRule.setText(resourceBundle.getString("CREATE_RULE"));
        exportRule.setText(resourceBundle.getString("EXPORT_RULE"));
        saveProject.setText(resourceBundle.getString("SAVE_PROJECT"));
        openProject.setText(resourceBundle.getString("OPEN_PROJECT"));
        searchBtn.setText(resourceBundle.getString("SEARCH"));
        exportDataBtn.setText(resourceBundle.getString("EXPORT_BUTTON"));
        queryString.setText(resourceBundle.getString("QUERY_CONTENT"));
        checkHoneyPot.setText(resourceBundle.getString("REMOVE_HONEYPOTS"));
        withFid.setText(resourceBundle.getString("WITH_FID"));
        isAll.setText(resourceBundle.getString("IS_ALL"));
        decoratedField = new AutoHintTextField(queryTF);
        client = DataUtil.loadConfigure();
        //初始化起始页tab
        Tab tab = this.tabPane.getTab(resourceBundle.getString("HOMEPAGE"));
        Button queryCert = new Button(resourceBundle.getString("QUERY_BUTTON"));
        Button queryFavicon = new Button(resourceBundle.getString(("QUERY_BUTTON")));
        Label label = new Label(resourceBundle.getString("CERT_LABEL"));
        Label faviconLabel = new Label(resourceBundle.getString("FAVICON_LABEL"));
        TextField tf = TextFields.createClearableTextField();
        TextField favionTF = TextFields.createClearableTextField();
//        Image image = new Image("api_doc_en.png");
        ImageView view = new ImageView(new Image(Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage()) ? "/images/api_doc_cn.png" : "/images/api_doc_en.png"));
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
                query("cert=\"" + i + "\"");
            }
        });
        queryFavicon.setOnAction(event -> {
            String url = favionTF.getText().trim();
            if(!url.isEmpty()){
                if(!url.startsWith("http")){
                    DataUtil.showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("ERROR_URL")).showAndWait();
                }else {
                    HashMap<String,String> res = helper.getImageFavicon(url);
                    if(res != null){
                        if(res.get("code").equals("error")){
                            DataUtil.showAlert(Alert.AlertType.ERROR, null, res.get("msg")).showAndWait();return;
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
//        tabPane.addTab(new Tab("test"), new TabDataBean(), "test");
//        tabPane.addTab(new Tab("test"), new TabDataBean(), "test");
//        tabPane.addTab(new Tab("test"), new TabDataBean(), "test");
    }

    /**
     * 查询按钮点击事件，与fxml中命名绑定
     */
    @FXML
    private void queryAction(){
        if(queryTF.getText() != null){
            query(queryTF.getText());
        }
    }

    /**
     * 关于 按钮
     */
    @FXML
    private void showAbout(){
        List<CommandLinksDialog.CommandLinksButtonType> clb = Arrays.asList(
                new CommandLinksDialog.CommandLinksButtonType("https://github.com/wgpsec/fofa_viewer",
                        resourceBundle.getString("ABOUT_HINT1"), true),
                new CommandLinksDialog.CommandLinksButtonType("https://github.com/wgpsec/fofa_viewer/issues",
                        resourceBundle.getString("ABOUT_HINT2"), true)
        );
        CommandLinksDialog dialog = new CommandLinksDialog(clb);
        dialog.setOnCloseRequest(e -> {
            ButtonType result = dialog.getResult();
            if(result.getButtonData() != ButtonBar.ButtonData.CANCEL_CLOSE){
                URI uri = URI.create(result.getText());
                Desktop dp = Desktop.getDesktop();
                if (dp.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        dp.browse(uri);
                    } catch (IOException ex) {
                        Logger.error(ex);
                    }
                }
            }
        });
        dialog.setTitle("Notice");
        dialog.setContentText("WgpSec Team");
        dialog.showAndWait();
    }

    /**
     * 配置设置
     */
    @FXML
    private void setConfig(){
        SetConfiDialog dialog = new SetConfiDialog(resourceBundle.getString("CONFIG_PANEL"));
        dialog.showAndWait();
    }
    /**
     * 打开项目
     */
    @FXML
    private void openProject(){
        if(isProject){
            Alert dialog = DataUtil.showAlert(Alert.AlertType.CONFIRMATION, null, resourceBundle.getString("OPEN_NEW_PROCESS"));
            dialog.setOnCloseRequest(event -> {
                ButtonType btn = dialog.getResult();
                if(btn.equals(ButtonType.OK)){

                }
            });
        }

    }

    /**
     * 保存项目
     */
    @FXML
    private void saveProject(){
        if(this.tabPane.getTabs().size() == 1){
            DataUtil.showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("SAVE_PROJECT_ERROR")).showAndWait();
        }else{
            SaveOptionDialog sd = new SaveOptionDialog(this.tabPane, true);
            sd.setOnCloseRequest(event -> {
                ButtonType rs = sd.getResult();
                if(rs.equals(ButtonType.OK)){
                    isProject = true;
                }
            });
            sd.showAndWait();
        }
    }

    /**
     * 创建规则
     */
    @FXML
    private void createRule(){
        if(this.tabPane.getTabs().size() == 1){
            DataUtil.showAlert(Alert.AlertType.WARNING, null, resourceBundle.getString("SAVE_RULE_ERROR")).showAndWait();
        }else{
            SaveOptionDialog sd = new SaveOptionDialog(this.tabPane, false);
            sd.showAndWait();
        }
    }

    /**
     * 导出规则
     */
    @FXML
    private void exportRule(){

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
                                DataUtil.showAlert(Alert.AlertType.ERROR, null, resourceBundle.getString("EXPORT_INPUT_NUM_HINT1")
                                        + totalPage + resourceBundle.getString("EXPORT_INPUT_NUM_HINT2")).showAndWait();
                                return;
                            }
                        }catch (NumberFormatException ex){
                            DataUtil.showAlert(Alert.AlertType.ERROR,null, resourceBundle.getString("EXPORT_INPUT_NUM_ERROR")).showAndWait();
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
                                String text = DataUtil.replaceString(tab.getText());
                                HashMap<String, String> result = helper.getHTML(client.getParam(String.valueOf(i), isAll.isSelected())
                                        + helper.encode(text), 50000, 50000);
                                if (result.get("code").equals("200")) {
                                    JSONObject obj = JSON.parseObject(result.get("msg"));
                                    DataUtil.loadJsonData(null, obj, totalData, urlList, true, null);
                                    updateMessage(resourceBundle.getString("LOADDATA_HINT1") + i + "/"
                                            + finalTotalPage + resourceBundle.getString("LOADDATA_HINT2"));
                                    updateProgress(i, finalTotalPage);
                                } else if (result.get("code").equals("error")) {
                                    // 请求失败时 等待1s再次请求
                                    Thread.sleep(1000);
                                    result = helper.getHTML(client.getParam(String.valueOf(i), isAll.isSelected())
                                            + helper.encode(text), 50000, 50000);
                                    if (result.get("code").equals("error")) {
                                        errorPage.append(i).append(" ");
                                        continue;
                                    }
                                    JSONObject obj = JSON.parseObject(result.get("msg"));
                                    DataUtil.loadJsonData(null, obj, totalData, urlList, true, null);
                                    updateMessage(resourceBundle.getString("LOADDATA_HINT1") + i + "/" + finalTotalPage
                                            + resourceBundle.getString("LOADDATA_HINT2"));
                                    updateProgress(i, finalTotalPage);
                                }
                            }
                        }catch (InterruptedException e){
                            Logger.error(e);
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
            DataUtil.exportToExcel(fileName, tab.getText(), totalData, urls, errorPage);
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
        final String queryText = text;
        if(this.tabPane.isExistTab(tabTitle)){ // 若已存在同名Tab 则直接跳转，不查询
            this.tabPane.setCurrentTab(this.tabPane.getTab(tabTitle));
            return;
        }
        for(CheckBox box : keyMap.keySet()){
            String name = keyMap.get(box);
            if(box.isSelected()){
                if(!client.fields.contains(name)){
                    client.fields.add(name);
                }
            }else{
                client.fields.remove(name);
            }
        }
        MainControllerCallback mCallback = new MainControllerCallback() {
            @Override
            public boolean getFidStatus() {
                return withFid.isSelected();
            }

            @Override
            public void queryCall(String queryTxt) {
                query(queryTxt);
            }

            @Override
            public void addSBListener(TableView<?> view) {
                addScrollBarListener(view);
            }
        };
        Tab tab = new Tab();
        tab.setOnCloseRequest(event -> tabPane.closeTab(tab));
        tab.setText(tabTitle);
        tab.setTooltip(new Tooltip(tabTitle));
        RequestBean bean = new RequestBean(client.getParam(null, isAll.isSelected())
                + helper.encode(queryText), tabTitle, client.getSize());
        new Request(new ArrayList<RequestBean>(){{add(bean);}}, new RequestCallback<Request>() {
            @Override
            public void before(TabDataBean tabDataBean) {
                tabPane.addTab(tab, tabDataBean, queryText);
                tabPane.setCurrentTab(tab);
                LoadingPane ld = new LoadingPane();
                tab.setContent(ld);
            }

            @Override
            public void succeeded(BorderPane tablePane, StatusBar bar) {
                if (bean.getResult().get("code").equals("200")) {
                    tab.setContent(tablePane);
                    decoratedField.addLog(bean.getTabTitle());
                    tabPane.addBar(tab, bar);
                } else {
                    ((LoadingPane)tab.getContent()).setErrorText("请求状态码："+bean.getResult().get("code")+ bean.getResult().get("msg"));
                }
            }

            @Override
            public void failed(String text) { // 网络问题请求失败
                ((LoadingPane)tab.getContent()).setErrorText(text);
            }
        }, mCallback).query();
    }

    /**
     * 设置滚动自动加载，需要等tableview加载完后设置
     * @param view tableview
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
        if(bar != null) {
            bar.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((double) newValue == 1.0D) {
                    Tab tab = tabPane.getCurrentTab();
                    TabDataBean bean = tabPane.getTabDataBean(tab);
                    if (bean.hasMoreData) {
                        bean.page += 1;
                        String text = DataUtil.replaceString(tab.getText());
                        Task<Void> task = new Task<Void>() {
                            @Override
                            protected Void call() {
                                HashMap<String, String> result = helper.getHTML(client.getParam(String.valueOf(bean.page),
                                        isAll.isSelected()) + helper.encode(text), 10000, 10000);
                                TableView<TableBean> tableView = (TableView<TableBean>) ((BorderPane) tab.getContent()).getCenter();
                                if (result.get("code").equals("200")) {
                                    JSONObject obj = JSON.parseObject(result.get("msg"));
                                    if (obj.getBoolean("error")) {
                                        return null;
                                    }
                                    Map<String, TableBean> list = (Map<String, TableBean>) DataUtil.loadJsonData(bean, obj, null, null, false, tableView);
                                    if (list.keySet().size() != 0) {
                                        ObservableList<TableBean> _tmp = tableView.getItems();
                                        TableBean b = _tmp.get(_tmp.size() - 5);
                                        List<TableBean> tmp = list.values().stream().sorted(Comparator.comparing(TableBean::getIntNum)).collect(Collectors.toList());
                                        Platform.runLater(() -> tableView.getItems().addAll(FXCollections.observableArrayList(tmp)));
                                        Platform.runLater(() -> tableView.scrollTo(b));
                                        StatusBar statusBar = tabPane.getBar(tab);
                                        Label countLabel = (Label) statusBar.getRightItems().get(2);
                                        Platform.runLater(() -> countLabel.setText(String.valueOf(Integer.parseInt(countLabel.getText()) + obj.getJSONArray("results").size())));
                                    }
                                    if (bean.page * Integer.parseInt(client.getSize()) > obj.getInteger("size")) {
                                        bean.hasMoreData = false;
                                    }
                                }
                                return null;
                            }
                        };
                        new Thread(task).start();
                    }
                }
            });
        }
    }

}
