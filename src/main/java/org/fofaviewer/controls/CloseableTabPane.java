package org.fofaviewer.controls;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.StatusBar;
import org.fofaviewer.bean.TabDataBean;
import org.fofaviewer.utils.ResourceBundleUtil;
import java.util.HashMap;

public class CloseableTabPane extends BorderPane {
    private final String homepage = ResourceBundleUtil.getResource().getString("HOMEPAGE");
    private final TabPane tabPane;
    private HashMap<Tab, TabDataBean> dataMap;
    private HashMap<Tab, StatusBar> barMap;

    public CloseableTabPane() {
        this.tabPane = new TabPane();
        this.dataMap = new HashMap<>();
        this.barMap = new HashMap<>();
        StackPane sp = new StackPane();
        sp.getChildren().add(tabPane);
        MenuButton menuButton = new MenuButton(ResourceBundleUtil.getResource().getString("CLOSE_OPTIONS"));
        menuButton.setVisible(false);
        StackPane.setMargin(menuButton,new Insets(5));
        sp.setAlignment(Pos.TOP_RIGHT);
        //Tab全部关闭后，则不显示关闭菜单按钮
        tabPane.getTabs().addListener(new ListChangeListener<Tab>() {
            @Override
            public void onChanged(Change<? extends Tab> c) {
                menuButton.setVisible(tabPane.getTabs().size() != 0);
            }
        });
        //关闭选中的Tab
        MenuItem closeSelected = new MenuItem(ResourceBundleUtil.getResource().getString("CLOSE_CHOOSE"));
        closeSelected.setOnAction(e->{
            for (Tab tab:tabPane.getTabs()){
                if(tab.getText().equals(homepage)){
                    continue;
                }
                if(tab.selectedProperty().getValue()){
                    tabPane.getTabs().remove(tab);
                    dataMap.remove(tab);
                    break;
                }
            }
        });
        menuButton.getItems().add(closeSelected);
        //关闭除选中的其他Tab
        MenuItem closeOthers = new MenuItem(ResourceBundleUtil.getResource().getString("CLOSE_OTHERS"));
        menuButton.getItems().add(closeOthers);
        closeOthers.setOnAction(e->{
            for (Tab tab:tabPane.getTabs()){
                if(tab.getText().equals(homepage)){ // 阻止关闭起始页
                    continue;
                }
                if(tab.selectedProperty().getValue()){
                    tabPane.getTabs().clear();
                    tabPane.getTabs().add(tab);
                    TabDataBean tmp = dataMap.get(tab);
                    dataMap.clear();
                    dataMap.put(tab, tmp);
                    break;
                }
            }
        });
        //关闭所有的Tab
        MenuItem closeAll = new MenuItem(ResourceBundleUtil.getResource().getString("CLOSE_ALL"));
        closeAll.setOnAction(e->{
            Tab tab = this.getTab(homepage);
            tabPane.getTabs().clear();
            dataMap.clear();
            this.addTab(tab, null);
        });
        menuButton.getItems().add(closeAll);
        sp.getChildren().add(menuButton);
        super.setCenter(sp);
        Tab tab = new Tab(homepage);
        this.addTab(tab, null);
        tab.setClosable(false); //取消启动页的关闭按钮
    }
    //添加单个Tab
    public void addTab(Tab tab, TabDataBean list){
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        if(list != null){
            dataMap.put(tab, list);
        }
    }
    //添加Tab集合
    public void addAll(ObservableList<Tab> tabs){
        for (Tab tab:tabs){
            tab.setClosable(true);
        }
        tabPane.getTabs().addAll(tabs);
    }

    public void addBar(Tab tab, StatusBar bar){
        barMap.put(tab, bar);
    }

    public StatusBar getBar(Tab tab){
        return barMap.get(tab);
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

    public TabDataBean getTabDataBean(Tab tab){
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
