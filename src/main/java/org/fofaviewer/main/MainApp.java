package org.fofaviewer.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.fofaviewer.bean.FofaBean;

public class MainApp extends Application {
    private FofaBean client;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fofa_viewer.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Fofa_Viewer v1.0.4 By f1ashine@WgpSec");
        stage.show();
        // 设置窗口的最小宽度和高度，避免自由伸缩太小不方便看
        stage.widthProperty().addListener((o, oldValue, newValue)->{
            if(newValue.intValue() < 1000.0) {
                stage.setResizable(false);
                stage.setWidth(1000);
                stage.setResizable(true);
            }
        });
        stage.heightProperty().addListener((o, oldValue, newValue)->{
            if(newValue.intValue() < 700.0) {
                stage.setResizable((false));
                stage.setHeight(700);
                stage.setResizable(true);
            }
        });
        //stage.setResizable(false); //禁止窗口拉伸
    }

    public static void main(String[] args) throws Exception{
        launch(args);
    }
}
