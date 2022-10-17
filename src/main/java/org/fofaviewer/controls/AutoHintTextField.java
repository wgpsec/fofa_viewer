package org.fofaviewer.controls;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.fofaviewer.utils.RequestUtil;
import org.fofaviewer.utils.SQLiteUtils;

import java.util.*;

public class AutoHintTextField {
    private final HashSet<String> historySet;
    private PopOver pop = new PopOver();
    private TextField textField;
    private final static int LIST_SHOW_SIZE = 7;
    private final static int LIST_CELL_HEIGHT = 24;
    private ObservableList<String> showCacheDataList = FXCollections.observableArrayList();
    private HashMap<String, String> tipMap = new HashMap<>();
    private final RequestUtil helper = RequestUtil.getInstance();
    private String clickedInput = "";
    private String inputText = "";
    private final ListView<String> histroyView;
    /** 输入内容后显示的提示信息列表 */
    private final ListView<String> autoTipList = new ListView<>();
    /** 输入内容后显示的pop */
    private final Popup popShowList = new Popup();

    public AutoHintTextField(TextField textField) {
        if (null == textField) {
            throw new RuntimeException("textField 不能为空");
        }
        //初始化查询历史界面
        historySet = new HashSet<>();
        pop.setAutoHide(true);
        histroyView = new ListView<>();
        VBox historyBox = new VBox();
        historyBox.getChildren().add(histroyView);
        histroyView.setItems(FXCollections.observableArrayList(historySet));
        pop.setContentNode(historyBox);
        this.textField = textField;
        popShowList.setAutoHide(true);
        popShowList.getContent().add(autoTipList);
        autoTipList.setItems(showCacheDataList);
        confListnenr();
    }

    public void addLog(String log){
        this.historySet.add(log);
        this.histroyView.setItems(FXCollections.observableArrayList(historySet));
    }

    public final Scene getScene() {
        return textField.getScene();
    }

    /**
     * 显示pop
     */
    public final void showTipPop() {
        autoTipList.setPrefWidth(textField.getWidth() - 3);
        if(showCacheDataList.size() < LIST_SHOW_SIZE) {
            autoTipList.setPrefHeight(showCacheDataList.size() * LIST_CELL_HEIGHT + 3);
        } else {
            autoTipList.setPrefHeight(LIST_SHOW_SIZE * LIST_CELL_HEIGHT + 3);
        }
        Window window = getWindow();
        Scene scene = getScene();
        Point2D fieldPosition = textField.localToScene(0, 0);
        popShowList.show(window, window.getX() + fieldPosition.getX() + scene.getX(),
                window.getY() + fieldPosition.getY() + scene.getY() + textField.getHeight());
        autoTipList.getSelectionModel().clearSelection();
        autoTipList.getFocusModel().focus(-1);
    }

    /**
     * 设置监听器
     */
    private void confListnenr() {
        textField.setOnMouseClicked(event -> pop.show(textField));

        histroyView.setOnMouseClicked(event -> {
            this.textField.setText(histroyView.getSelectionModel().getSelectedItem());
            this.pop.hide();
        });

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5)); // 延时0.5秒查询api
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            inputText = newValue;
            pop.hide();
            pause.setOnFinished(event -> {
                if(inputText.equals(newValue)){ // 避免在输入中查询，尽量保持在输出结束后查询关联
                    updateCacheDataList(oldValue, newValue);
                }
            });
            pause.playFromStart();
        });

        autoTipList.setOnMouseClicked(event -> selectedItem());

        autoTipList.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                selectedItem();
            }
        });
    }

    /**
     * 选中条目点击事件
     */
    private void selectedItem() {
        clickedInput = tipMap.get(autoTipList.getSelectionModel().getSelectedItem());
        textField.setText(clickedInput);
        textField.end();
        popShowList.hide();
    }

    /**
     * 输入内容改变触发事件
     * @param oldValue 旧值
     * @param newValue 新值
     */
    private void updateCacheDataList(String oldValue, String newValue){
        this.pop.hide();
        if(newValue.trim().equals("")){ // 内容为空时不查询
            showCacheDataList.clear();
            tipMap.clear();
            return;
        }
        if(newValue.trim().equals(oldValue)){ // 键入空格不查询
            showTipPop();
            return;
        }
        if(clickedInput.equals(newValue)){ // 消除点击条目后自动触发的bug
            return;
        }
        showCacheDataList.clear();
        tipMap.clear();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                Map<String,String> ruleData = SQLiteUtils.matchRule(newValue);
                Map<String,String> tipData = helper.getTips(newValue);
                tipMap.putAll(ruleData);
                List<String> rdata = new ArrayList<>(ruleData.keySet());
                List<String> tdata = new ArrayList<>();
                if(tipData != null) {
                    tipMap.putAll(tipData);
                    tdata.addAll(tipData.keySet());
                }
                List<String> data;
                if(rdata.size() != 0){
                    rdata.addAll(tdata);
                    data = rdata;
                }else{
                    data = tdata;
                }
                List<String> list = data;
                if(data.size() != 0){
                    Platform.runLater(() -> {
                        showCacheDataList.addAll(list);
                        showTipPop();
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public final Window getWindow() {
        return getScene().getWindow();
    }
}
