package org.fofaviewer.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;
import org.fofaviewer.controllers.SaveOptionsController;
import org.tinylog.Logger;
import java.io.IOException;

public class SaveOptionDialog extends Dialog<Void> {

    public SaveOptionDialog(CloseableTabPane tablePane)  {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SaveOptionsDialog.fxml"));
            Pane pane = loader.load();
            getDialogPane().setContent(pane);
            SaveOptionsController controller = loader.getController();
            controller.setTabs(tablePane);
        }catch (IOException e){
            Logger.error(e);
        }
    }
}
