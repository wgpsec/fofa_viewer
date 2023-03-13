package org.fofaviewer.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.fofaviewer.main.FofaConfig;
import org.fofaviewer.main.ProxyConfig;
import org.fofaviewer.utils.DataUtil;
import org.fofaviewer.utils.ResourceBundleUtil;
import org.fofaviewer.utils.SQLiteUtils;
import org.tinylog.Logger;
import java.io.*;
import java.util.*;

public class SetConfigDialogController {
    private File configFile;
    private Map<TextField, String> propertiesMap;
    private FofaConfig fofaConfig;
    private ProxyConfig proxyConfig;
    private ResourceBundle bundle;
    @FXML
    private Tab fofa_tab;
    @FXML
    private Tab proxy_tab;
    @FXML
    private Label label_fofa_api;
    @FXML
    private Label label_fofa_email;
    @FXML
    private Label label_fofa_key;
    @FXML
    private Label label_fofa_max_size;
    @FXML
    private Label labelCombo;
    @FXML
    private Label label_proxy_ip;
    @FXML
    private Label label_proxy_port;
    @FXML
    private Label label_proxy_user;
    @FXML
    private Label label_proxy_password;
    @FXML
    private TextField fofa_api;
    @FXML
    private TextField fofa_email;
    @FXML
    private TextField fofa_key;
    @FXML
    private TextField fofa_max_size;
    @FXML
    private ComboBox typeCombo;
    @FXML
    private TextField proxy_ip;
    @FXML
    private TextField proxy_port;
    @FXML
    private TextField proxy_user;
    @FXML
    private TextField proxy_password;
    @FXML
    private RadioButton enable;
    @FXML
    private RadioButton disable;
    @FXML
    private Label checkLeftAmount;
    @FXML
    private RadioButton enableCheck;
    @FXML
    private RadioButton disableCheck;
    @FXML
    private void initialize(){
        bundle = ResourceBundleUtil.getResource();
        fofa_tab.setText(bundle.getString("FOFA_CONFIG"));
        proxy_tab.setText(bundle.getString("PROXY_CONFIG"));
        enable.setText(bundle.getString("ENABLE_RADIO"));
        checkLeftAmount.setText(bundle.getString("CHECK_LEFT_AMOUNT"));
        enableCheck.setText(bundle.getString("ENABLE_RADIO"));
        disableCheck.setText(bundle.getString("DISABLE_RADIO"));
        disable.setText(bundle.getString("DISABLE_RADIO"));
        label_fofa_api.setText(bundle.getString("FOFA_API"));
        label_fofa_email.setText(bundle.getString("FOFA_EMAIL"));
        label_fofa_key.setText(bundle.getString("FOFA_KEY"));
        label_fofa_max_size.setText(bundle.getString("FOFA_MAX_SIZE"));
        label_proxy_ip.setText(bundle.getString("PROXY_IP_ADDRESS"));
        label_proxy_port.setText(bundle.getString("PROXY_PORT"));
        label_proxy_user.setText(bundle.getString("PROXY_USER"));
        label_proxy_password.setText(bundle.getString("PROXY_PASSWORD"));
        labelCombo.setText(bundle.getString("PROXY_TYPE"));
        propertiesMap = new HashMap<TextField, String>(){{
            put(fofa_api, "api");
            put(fofa_email, "email");
            put(fofa_key, "key");
            put(fofa_max_size, "max_size");
            put(proxy_ip, "proxy_ip");
            put(proxy_port, "proxy_port");
            put(proxy_user, "proxy_user");
            put(proxy_password, "proxy_password");
        }};
        ToggleGroup statusGroup = new ToggleGroup();
        enable.setToggleGroup(statusGroup);
        disable.setToggleGroup(statusGroup);
        statusGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> proxyConfig.setStatus(statusGroup.getSelectedToggle().equals(enable)));
        ToggleGroup checkStatusGroup = new ToggleGroup();
        enableCheck.setToggleGroup(checkStatusGroup);
        disableCheck.setToggleGroup(checkStatusGroup);
        checkStatusGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> fofaConfig.setCheckStatus(checkStatusGroup.getSelectedToggle().equals(enableCheck)));
        fofaConfig = FofaConfig.getInstance();
        proxyConfig = ProxyConfig.getInstance();
        typeCombo.setItems(FXCollections.observableArrayList(ProxyConfig.ProxyType.HTTP, ProxyConfig.ProxyType.SOCKS5));
        typeCombo.getSelectionModel().select(0);
        createConfigFile();
        loadConfigFile();
    }

    public void setAction(DialogPane dialogPane){
        dialogPane.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, e -> {
            createConfigFile();
            if(enable.isSelected() && (proxy_ip.getText().equals("") || proxy_port.getText().equals(""))){
                DataUtil.showAlert(Alert.AlertType.WARNING, null, bundle.getString("PROXY_SET_ERROR")).showAndWait();
                e.consume();
            }else{
                Properties properties = new Properties();
                try {
                    fofaConfig.setEmail(fofa_email.getText());
                    fofaConfig.API = fofa_api.getText();
                    fofaConfig.setSize(fofa_max_size.getText());
                    fofaConfig.setKey(fofa_key.getText());
                    fofaConfig.setCheckStatus(enableCheck.isSelected());
                    proxyConfig.setStatus(enable.isSelected());
                    proxyConfig.setProxy_ip(proxy_ip.getText());
                    proxyConfig.setProxy_port(proxy_port.getText());
                    proxyConfig.setProxy_user(proxy_user.getText());
                    proxyConfig.setProxy_password(proxy_password.getText());
                    FileOutputStream outputStream = new FileOutputStream(this.configFile);
                    for(TextField tf : propertiesMap.keySet()){
                        properties.setProperty(propertiesMap.get(tf), tf.getText());
                    }
                    properties.setProperty("proxy_status", proxyConfig.getStatus() ? "on" : "off");
                    properties.setProperty("check_status", fofaConfig.getCheckStatus() ? "on" : "off");
                    ProxyConfig.ProxyType type = (ProxyConfig.ProxyType) typeCombo.getSelectionModel().getSelectedItem();
                    switch (type){
                        case HTTP: properties.setProperty("proxy_type", "HTTP");break;
                        case SOCKS5: properties.setProperty("proxy_type", "SOCKS5");break;
                    }
                    properties.store(outputStream, "config.properties");
                    outputStream.close();
                } catch (IOException ex) {
                    Logger.error(ex);
                }
            }
        });
    }

    private void createConfigFile(){
        String filePath = SQLiteUtils.getPath() + "config.properties";
        try{
            File f = new File(filePath);
            this.configFile = f;
            if(!f.exists()){
                f.createNewFile();
            }
        }catch (IOException e){
            Logger.error(e);
        }
    }

    private void loadConfigFile(){
        this.fofa_api.setText(fofaConfig.API);
        this.fofa_email.setText(fofaConfig.getEmail());
        this.fofa_key.setText(fofaConfig.getKey());
        this.fofa_max_size.setText(fofaConfig.getSize());
        if(proxyConfig.getStatus()){
            enable.setSelected(true);
        }else{
            disable.setSelected(true);
        }
        if(fofaConfig.getCheckStatus()){
            enableCheck.setSelected(true);
        }else{
            disableCheck.setSelected(true);
        }
        this.proxy_ip.setText(proxyConfig.getProxy_ip());
        this.proxy_port.setText(proxyConfig.getProxy_port());
        this.proxy_user.setText(proxyConfig.getProxy_user());
        this.proxy_password.setText(proxyConfig.getProxy_password());
    }
}
