package org.fofaviewer.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Pane;
import org.fofaviewer.controllers.SetConfigDialogController;
import org.tinylog.Logger;
import java.io.IOException;

public class SetConfiDialog extends Dialog<ButtonType> {
    public SetConfiDialog(String title) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SetConfigDialog.fxml"));
            Pane pane = loader.load();
            this.setTitle(title);
            DialogPane dialogPane = this.getDialogPane();
            dialogPane.setContent(pane);
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            SetConfigDialogController controller = loader.getController();
            controller.setAction(dialogPane);
        }catch (IOException e){
            Logger.error(e);
        }
    }
}
