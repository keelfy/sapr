package keelfy.sapr.controller;

import javafx.scene.Scene;
import javafx.stage.Stage;
import keelfy.sapr.dto.SaprScene;
import keelfy.sapr.util.Utility;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author e.kuzmin
 * */
public class MainMenuController implements IController {

    public void openPreProcessor() throws IOException {
        openScene(SaprScene.PRE_PROCESSOR);
    }

    public void openProcessor() throws IOException {
        openScene(SaprScene.PROCESSOR);
    }

    public void openPostProcessor() throws IOException {
        openScene(SaprScene.POST_PROCESSOR);
    }

    private void openScene(@NonNull SaprScene saprScene) throws IOException {
        final var stage = new Stage();
        final var root = Utility.loadScene(saprScene);
        final var scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(saprScene.getTitle());
        stage.show();
    }
}
