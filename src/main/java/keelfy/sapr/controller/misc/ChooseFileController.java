package keelfy.sapr.controller.misc;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import keelfy.sapr.controller.IController;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * @author e.kuzmin
 */
public class ChooseFileController implements IController {

    @Setter
    private Consumer<String> okClickedAction = (s) -> {};

    @FXML
    private TextField fileNameTextField;

    public void okButtonClicked(ActionEvent actionEvent) {
        String fileName = fileNameTextField.getText();
        okClickedAction.accept(fileName + ".json");
        final var  source = (Node)  actionEvent.getSource();
        final var stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
