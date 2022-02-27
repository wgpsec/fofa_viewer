package org.fofaviewer.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Pane;
import org.fofaviewer.callback.SaveOptionCallback;
import org.fofaviewer.controllers.SaveOptionsController;
import org.tinylog.Logger;
import java.io.IOException;

public class SaveOptionDialog extends Dialog<ButtonType> {

    public SaveOptionDialog(CloseableTabPane tablePane, boolean isProject, SaveOptionCallback callback)  {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SaveOptionsDialog.fxml"));
            Pane pane = loader.load();
            DialogPane dialogPane = this.getDialogPane();
            dialogPane.setContent(pane);
            SaveOptionsController controller = loader.getController();
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            controller.setProject(isProject, dialogPane, callback);
            controller.setTabs(tablePane);
        }catch (IOException e){
            Logger.error(e);
        }
    }
}
