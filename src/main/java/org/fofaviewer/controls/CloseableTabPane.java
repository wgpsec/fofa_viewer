package org.fofaviewer.controls;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import java.util.ArrayList;
import java.util.HashMap;

public class CloseableTabPane extends BorderPane {
    private final TabPane tabPane;
    private HashMap<Tab, ArrayList<String>> dataMap;

    public CloseableTabPane() {
        this.tabPane = new TabPane();
        this.dataMap = new HashMap<>();
        StackPane sp = new StackPane();
        sp.getChildren().add(tabPane);
        MenuButton menuButton = new MenuButton("关闭选项");
        menuButton.setVisible(false);
        StackPane.setMargin(menuButton,new Insets(5));
        sp.setAlignment(Pos.TOP_RIGHT);
        //Tab全部关闭后，则不显示关闭菜单按钮
        tabPane.getTabs().addListener(new ListChangeListener<Tab>() {
            @Override
            public void onChanged(Change<? extends Tab> c) {
                if(tabPane.getTabs().size()==0){
                    menuButton.setVisible(false);
                }else {
                    menuButton.setVisible(true);
                }
            }
        });
        //关闭选中的Tab
        MenuItem closeSelected = new MenuItem("关闭选中");
        closeSelected.setOnAction(e->{
            for (Tab tab:tabPane.getTabs()){
                if(tab.selectedProperty().getValue()){
                    tabPane.getTabs().remove(tab);
                    dataMap.remove(tab);
                    break;
                }
            }
        });
        menuButton.getItems().add(closeSelected);
        //关闭除选中的其他Tab
        MenuItem closeOthers = new MenuItem("关闭其他");
        menuButton.getItems().add(closeOthers);
        closeOthers.setOnAction(e->{
            for (Tab tab:tabPane.getTabs()){
                if(tab.selectedProperty().getValue()){
                    tabPane.getTabs().clear();
                    tabPane.getTabs().add(tab);
                    ArrayList<String> tmp = dataMap.get(tab);
                    dataMap.clear();
                    dataMap.put(tab, tmp);
                    break;
                }
            }
        });
        //关闭所有的Tab
        MenuItem closeAll = new MenuItem("关闭所有");
        closeAll.setOnAction(e->{
            tabPane.getTabs().clear();
            dataMap.clear();
        });
        menuButton.getItems().add(closeAll);
        sp.getChildren().add(menuButton);
        super.setCenter(sp);
        Tab tab = new Tab("启动页");
        this.addTab(tab, new ArrayList<String>(0));
        tab.setClosable(false); //取消启动页的关闭按钮
    }
    //添加单个Tab
    public void addTab(Tab tab, ArrayList<String> list){
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        if(list != null){
            dataMap.put(tab, list);
        }else{
            dataMap.put(tab, new ArrayList<String>());
        }
    }
    //添加Tab集合
    public void addAll(ObservableList<Tab> tabs){
        for (Tab tab:tabs){
            tab.setClosable(true);
        }
        tabPane.getTabs().addAll(tabs);
    }

    public boolean isExistTab(String name){
        for(Tab tab : this.tabPane.getTabs()){
            if(tab.getText().equals(name)){
                return true;
            }
        }
        return false;
    }

    public Tab getTab(String name){
        for(Tab tab : this.tabPane.getTabs()){
            if(tab.getText().equals(name)){
                return tab;
            }
        }
        return null;
    }

    public ArrayList<String> getList(Tab tab){
        return dataMap.get(tab);
    }
    /**
     * 设置tabPane当前显示的Pane
     */
    public void setCurrentTab(Tab tab){
        this.tabPane.getSelectionModel().select(tab);
    }

    public Tab getCurrentTab(){
        return this.tabPane.getSelectionModel().getSelectedItem();
    }
}
