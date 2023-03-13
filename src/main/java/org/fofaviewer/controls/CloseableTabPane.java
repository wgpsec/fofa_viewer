package org.fofaviewer.controls;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.StatusBar;
import org.fofaviewer.bean.TabDataBean;
import org.fofaviewer.callback.MainControllerCallback;
import org.fofaviewer.utils.ResourceBundleUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CloseableTabPane extends BorderPane {
    private final String homepage = ResourceBundleUtil.getResource().getString("HOMEPAGE");
    private final TabPane tabPane;
    // tab页的数据存储，主要是
    private final HashMap<Tab, TabDataBean> dataMap;
    // tab页下方的状态栏
    private final HashMap<Tab, StatusBar> barMap;
    private final HashMap<Tab, String> queryMap;
    private MainControllerCallback callback;

    public CloseableTabPane() {
        this.tabPane = new TabPane();
        this.dataMap = new HashMap<>();
        this.barMap = new HashMap<>();
        this.queryMap = new HashMap<>();
        this.tabPane.tabMaxWidthProperty().set(150);
        tabPane.getStylesheets().add(getClass().getResource("/tabpane.css").toExternalForm());
        StackPane sp = new StackPane();
        sp.getChildren().add(tabPane);
        MenuButton menuButton = new MenuButton(ResourceBundleUtil.getResource().getString("CLOSE_OPTIONS"));
        menuButton.setVisible(false);
        StackPane.setMargin(menuButton,new Insets(5));
        sp.setAlignment(Pos.TOP_LEFT);
        //Tab全部关闭后，则不显示关闭菜单按钮
        tabPane.getTabs().addListener((ListChangeListener<Tab>) c -> menuButton.setVisible(tabPane.getTabs().size() != 0));
        //关闭选中的Tab
        MenuItem closeSelected = new MenuItem(ResourceBundleUtil.getResource().getString("CLOSE_CHOOSE"));
        closeSelected.setOnAction(e->{
            Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
            if(!tab.getText().equals(homepage)){
                tabPane.getTabs().remove(tab);
                dataMap.remove(tab);
            }
        });
        //关闭除选中的其他Tab
        MenuItem closeOthers = new MenuItem(ResourceBundleUtil.getResource().getString("CLOSE_OTHERS"));
        closeOthers.setOnAction(e->{
            if(tabPane.getTabs().size()==1) return;
            Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
            Tab home = this.getTab(homepage);
            if(tab.equals(home)) return;
            tabPane.getTabs().clear();
            tabPane.getTabs().addAll(home, tab);
            TabDataBean tmp = dataMap.get(tab);
            dataMap.clear();
            dataMap.put(tab, tmp);
        });
        //关闭所有的Tab
        MenuItem closeAll = new MenuItem(ResourceBundleUtil.getResource().getString("CLOSE_ALL"));
        closeAll.setOnAction(e->{
            if(tabPane.getTabs().size()==1) return;
            Tab tab = this.getTab(homepage);
            tabPane.getTabs().clear();
            dataMap.clear();
            tabPane.getTabs().add(tab);
        });
        //重新加载tab
        MenuItem reload = new MenuItem(ResourceBundleUtil.getResource().getString("RELOAD_TAB"));
        reload.setOnAction(e->{
            Tab tab = this.tabPane.getSelectionModel().getSelectedItem();
            if(tab.getText().equals(homepage)){
                return;
            }
            this.tabPane.getTabs().remove(tab);
            callback.queryCall(new ArrayList<String>(){{add(tab.getText());}});
        });
        menuButton.getItems().addAll(closeOthers, closeSelected, closeAll, reload);
        sp.getChildren().add(menuButton);
        super.setCenter(sp);
        Tab tab = new Tab(homepage);
        tab.setTooltip(new Tooltip(tab.getText()));
        this.addTab(tab, null, null);
        tab.setClosable(false); //取消启动页的关闭按钮
    }
    //添加单个Tab
    public void addTab(Tab tab, TabDataBean list, String queryUrl){
        if(tabPane.getTabs().size()!=0 && tab.getText().equals(homepage)) {
            return;
        }
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        if(list != null){
            dataMap.put(tab, list);
        }
        queryMap.put(tab, queryUrl);
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

    public ArrayList<Tab> getTabs(){
        return new ArrayList<>(this.tabPane.getTabs());
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

    public String getCurrentQuery(Tab tab){
        return this.queryMap.get(tab);
    }

    public Tab getCurrentTab(){
        return this.tabPane.getSelectionModel().getSelectedItem();
    }

    public void closeTab(Tab tab){
        this.dataMap.remove(tab);
        this.barMap.remove(tab);
        this.queryMap.remove(tab);
    }

    public Collection<String> getTabsTxt(){
        return this.queryMap.values();
    }

    public void setCallback(MainControllerCallback callback){
        this.callback = callback;
    }
}
