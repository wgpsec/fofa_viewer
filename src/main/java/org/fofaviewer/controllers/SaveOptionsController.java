package org.fofaviewer.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.fofaviewer.controls.CloseableTabPane;
import java.util.ArrayList;

public class SaveOptionsController {
    private ArrayList<Tab> tabs;
    @FXML
    private BorderPane window;
    @FXML
    private HBox root;
    @FXML
    private Label title;
    @FXML
    private Button selectAll;
    @FXML
    private Button unselect;
    @FXML
    private Button confirm;
    @FXML
    private Button cancel;
    @FXML
    private void initialize(){
        title.setText("请选择需要保存的查询条件");
        selectAll.setText("全选");
        unselect.setText("反选");
        confirm.setText("确认");
        cancel.setText("取消");
    }

    public void setTabs(CloseableTabPane tabPane){
        this.tabs = tabPane.getTabs();
    }

    @FXML
    private void closeWindow(){
        ((Stage)this.window.getScene().getWindow()).close();
    }

}
