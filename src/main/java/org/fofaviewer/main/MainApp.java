package org.fofaviewer.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.fofaviewer.controls.SetConfiDialog;

public class MainApp extends Application {
    private String version = "1.1.5";

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fofa_viewer.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Fofa_Viewer v" + this.version+ " By f1ashine@WgpSec");
        stage.show();
        stage.setMinWidth(1100);
        stage.setMinHeight(800);
    }

    public static void main(String[] args){
        launch(args);
    }
}
