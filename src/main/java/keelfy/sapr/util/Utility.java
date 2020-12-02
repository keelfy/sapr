package keelfy.sapr.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import keelfy.sapr.Sapr;
import keelfy.sapr.controller.IController;
import keelfy.sapr.controller.misc.ChooseFileController;
import keelfy.sapr.dto.AlertDto;
import keelfy.sapr.dto.SaprScene;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author e.kuzmin
 */
@UtilityClass
public class Utility {

    public Parent loadScene(@NonNull SaprScene saprScene) throws IOException {
        final var path = String.format("/%s.fxml", saprScene.getFileName());
        final var fxmlLoader = new FXMLLoader();
        final Parent root = fxmlLoader.load(Sapr.class.getResource(path).openStream());
        ((IController) fxmlLoader.getController()).afterLoad();
        return root;
    }

    public void showAlert(AlertDto alertDto) {
        final var alert = new Alert(alertDto.getAlertType());
        alert.setTitle(alertDto.getTitle());
        alert.setHeaderText(alertDto.getHeader());
        alert.setContentText(alertDto.getContent());
        alert.showAndWait();
    }

    public double parseDouble(TextField textField) {
        return Double.parseDouble(textField.getText());
    }

    public boolean isTextFieldNumbersValid(TextField... textFields) {
        if (Arrays.stream(textFields).map(TextField::getText).anyMatch(Utility::isNotValidNumber)) {
            showAlert(new AlertDto()
                    .setHeader("Неправильный ввод")
                    .setContent("Значения не могут быть равны 0!"));
            return false;
        }
        return true;
    }

    private boolean isNotValidNumber(String number) {
        return !NumberUtils.isCreatable(number) || Double.parseDouble(number) == 0;
    }

    public void openFileDialog(Consumer<String> action) throws IOException {
        final var path = String.format("/%s.fxml", SaprScene.CHOOSE_FILE.getFileName());
        final var fxmlLoader = new FXMLLoader();
        final Parent root = fxmlLoader.load(Sapr.class.getResource(path).openStream());
        ((ChooseFileController) fxmlLoader.getController()).setOkClickedAction(action);

        final var stage = new Stage();
        final var scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(SaprScene.CHOOSE_FILE.getTitle());
        stage.show();
    }

}
