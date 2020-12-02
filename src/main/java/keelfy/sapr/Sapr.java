package keelfy.sapr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import lombok.Getter;

import java.io.IOException;

/**
 * @author e.kuzmin
 * */
public class Sapr extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        final var mainLoader = new FXMLLoader();
        final var mainXmlUrl = getClass().getResource("/main_menu.fxml");
        mainLoader.setLocation(mainXmlUrl);
        final Parent mainWindow = mainLoader.load();
        stage.setScene(new Scene(mainWindow));
        stage.setTitle("Sapr by Egor Kuzmin");
        stage.show();
    }
}


