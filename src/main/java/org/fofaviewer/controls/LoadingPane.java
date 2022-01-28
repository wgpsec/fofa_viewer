package org.fofaviewer.controls;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * 错误信息展示Pane，方便数据加载过渡
 */
public class LoadingPane extends BorderPane{
    private final VBox box = new VBox();
    private final HBox hbox = new HBox();
    private final ImageView loadingView = new ImageView(new Image("/images/loading.gif"));
    private final Label header = new Label("");

    public LoadingPane(){
        super();
        this.setMouseTransparent(true);
        this.setPadding(Insets.EMPTY);
        this.header.setFont(new Font(30));
        this.header.setText("加载中...");
        this.setCenter(box);
        box.setPadding(new Insets(100,0,0,0));
        box.setAlignment(Pos.TOP_CENTER);
        hbox.getChildren().add(loadingView);
        hbox.setAlignment(Pos.CENTER);
        box.getChildren().addAll(header,hbox);
        hbox.setPadding(new Insets(0,0,0,100));
    }

    public void setErrorText(String info){
        this.header.setText("错误信息：");
        box.getChildren().remove(hbox);
        Label label = new Label(info);
        label.setFont(new Font(20));
        box.getChildren().add(label);
    }

    public void setLoadingView(){
        this.header.setText("加载中...");
        this.box.getChildren().clear();
        box.getChildren().add(hbox);
    }

}
