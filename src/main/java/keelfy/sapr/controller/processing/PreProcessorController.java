package keelfy.sapr.controller.processing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import keelfy.sapr.controller.IController;
import keelfy.sapr.controller.SaprController;
import keelfy.sapr.dto.AlertDto;
import keelfy.sapr.dto.Bar;
import keelfy.sapr.dto.Force;
import keelfy.sapr.dto.Load;
import keelfy.sapr.service.Painter;
import keelfy.sapr.util.Utility;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

/**
 * @author e.kuzmin
 * */
public class PreProcessorController extends SaprController implements IController {

    private static final String REGEX_WRONG_SYMBOLS = "\\d\\d*\\.?\\d*[e,E]?-?\\d*";
    private static final String REGEX_WRONG_SYMBOLS_MINUS = "-?\\d\\d*\\.?\\d*[e,E]?-?\\d*";

    private int selectedBar = -1;
    private int selectedNode = -1;

    @FXML
    private TextField elasticityTextField;

    @FXML
    private TextField areaTextField;

    @FXML
    private TextField lengthTextField;

    @FXML
    private TextField sigmaTextField;

    @FXML
    private TextField loadTextField;

    @FXML
    private TextField forceTextField;

    @FXML
    private TableView<Bar> barTable;

    @FXML
    private TableColumn<Bar, Integer> barIdColumn;

    @FXML
    private TableColumn<Bar, Double> elasticityColumn;

    @FXML
    private TableColumn<Bar, Double> areaColumn;

    @FXML
    private TableColumn<Bar, Double> lengthColumn;

    @FXML
    private TableColumn<Bar, Double> sigmaColumn;

    @FXML
    private ComboBox<Integer> loadComboBox;

    @FXML
    private ComboBox<Integer> nodeComboBox;

    @FXML
    private TableView<Force> forceTable;

    @FXML
    private TableColumn<Force, Integer> forceIdColumn;

    @FXML
    private TableColumn<Force, Double> forceValueColumn;

    @FXML
    private TableView<Load> loadTable;

    @FXML
    private TableColumn<Load, Integer> loadIdColumn;

    @FXML
    private TableColumn<Load, Double> loadValueColumn;

    @FXML
    private CheckBox leftTermination;

    @FXML
    private CheckBox rightTermination;

    @Override
    public void afterLoad() {
        addTextMatcher(elasticityTextField, REGEX_WRONG_SYMBOLS);
        addTextMatcher(areaTextField, REGEX_WRONG_SYMBOLS);
        addTextMatcher(lengthTextField, REGEX_WRONG_SYMBOLS);
        addTextMatcher(sigmaTextField, REGEX_WRONG_SYMBOLS);
        addTextMatcher(loadTextField, REGEX_WRONG_SYMBOLS_MINUS);
        addTextMatcher(forceTextField, REGEX_WRONG_SYMBOLS_MINUS);
    }

    private void addTextMatcher(TextField textField, String wrongSymbolsRegex) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(wrongSymbolsRegex)) {
                textField.setText(oldValue);
            }
        });
    }

    private void updateComboBoxes() {
        nodeComboBox.getItems().clear();
        loadComboBox.getItems().clear();
        IntStream.range(0, getBars().size()).forEach(nodeComboBox.getItems()::add);
        IntStream.range(0, getBars().size()).forEach(loadComboBox.getItems()::add);
    }

    private void addBarToTable(@NonNull Bar bar) {
        barTable.getItems().add(bar);
        barIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        elasticityColumn.setCellValueFactory(new PropertyValueFactory<>("elasticity"));
        areaColumn.setCellValueFactory(new PropertyValueFactory<>("area"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        sigmaColumn.setCellValueFactory(new PropertyValueFactory<>("sigma"));
    }

    private void addForceToTable(@NonNull Force force) {
        forceTable.getItems().add(force);
        forceIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        forceValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    }

    private void addLoadToTable(@NonNull Load load) {
        loadTable.getItems().add(load);
        loadIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        loadValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    }

    @FXML
    private void addBarClicked() {
        if (!Utility.isTextFieldNumbersValid(areaTextField, elasticityTextField, lengthTextField, sigmaTextField)) {
            return;
        }

        final var area = Utility.parseDouble(areaTextField);
        final var elasticity = Utility.parseDouble(elasticityTextField);
        final var length = Utility.parseDouble(lengthTextField);
        final var sigma = Utility.parseDouble(sigmaTextField);
        final var bar = new Bar(countBars(), area, elasticity, length, sigma);

        addBar(bar);
        addBarToTable(bar);

        updateComboBoxes();
    }

    @FXML
    public void nodeCBoxClicked() {
        try {
            selectedNode = nodeComboBox.getValue();
        } catch (Exception ex) {
            selectedNode = -1;
        }
    }

    @FXML
    public void removeBarClicked() {
        final var lastBarId = getBars()
                .keySet()
                .stream()
                .sorted()
                .reduce((bar, bar2) -> bar2)
                .orElse(null);

        if (lastBarId == null) {
            return;
        }

        barTable.getItems().removeIf(bar -> bar.getId() == lastBarId);
        forceTable.getItems().removeIf(force -> force.getId() == lastBarId);
        loadTable.getItems().removeIf(load -> load.getId() == lastBarId);

        forceTable.refresh();
        loadTable.refresh();

        removeBar(lastBarId);
        removeForce(lastBarId);
        removeLoad(lastBarId);

        updateComboBoxes();
    }

    @FXML
    public void addForceClicked() {
        if (selectedNode < 0) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Нужно выбрать узел")
                    .setContent("Пожалуйста, выберите узел!"));
            return;
        }

        if (!Utility.isTextFieldNumbersValid(forceTextField)) {
            return;
        }

        final var forceValue = Double.parseDouble(forceTextField.getText());
        final var force = new Force(selectedNode, forceValue);

        if (getForces().containsKey(selectedNode)) {
            forceTable.getItems().removeIf(force1 -> force1.getId() == selectedNode);
        }

        addForce(force);
        addForceToTable(force);
        forceTable.refresh();
    }

    @FXML
    public void loadCBoxClicked() {
        try {
            selectedBar = loadComboBox.getValue();
        } catch (Exception ex) {
            selectedBar = -1;
        }
    }

    @FXML
    public void addLoadClicked() {
        if (selectedBar < 0) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Выберите стержень")
                    .setContent("Пожалуйста, выберите стержень!"));
            return;
        }

        if (!Utility.isTextFieldNumbersValid(loadTextField)) {
            return;
        }

        final var loadValue = Double.parseDouble(loadTextField.getText());
        final var load = new Load(selectedBar, loadValue);

        if (getLoads().containsKey(selectedBar)) {
            loadTable.getItems().removeIf(load1 -> load1.getId() == selectedBar);
        }

        addLoad(load);
        addLoadToTable(load);
        loadTable.refresh();
    }

    @FXML
    public void leftTerminationClicked() {
        super.setLeftBorder(leftTermination.isSelected());
    }

    @FXML
    public void rightTerminationClicked() {
        super.setRightBorder(rightTermination.isSelected());
    }

    public boolean isValidBorders() {
        if (!leftTermination.isSelected() && !rightTermination.isSelected()) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Неправильное количество жестких заделок!")
                    .setContent("Пожалуйста, выберите как минимум 1 жесткую заделку!"));
            return false;
        }
        return true;
    }

    public void drawButtonClicked(){
        if (!isValidBorders() || getBars().isEmpty()) {
            return;
        }

        Painter.create(getBars(), getForces(), getLoads())
                .setEnableLeftBorder(leftTermination.isSelected())
                .setEnableRightBorder(rightTermination.isSelected())
                .draw();
    }

    private void saveIntoFile(String fileName) {
        final var objectMapper = new ObjectMapper();
        final var projectFile = new File(fileName);

        super.setLeftBorder(this.leftTermination.isSelected());
        super.setRightBorder(this.rightTermination.isSelected());
        final var root = convertIntoJsonNode(objectMapper);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(projectFile, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveButtonClicked() throws IOException {
        if (!isValidBorders()) {
            return;
        }

        Utility.openFileDialog(this::saveIntoFile);
    }

    private void loadFromFile(String fileName) {
        final var file = new File(fileName);
        final var objectMapper = new ObjectMapper();

        JsonNode root;
        try {
            root = objectMapper.readTree(file);
        } catch(IOException ex) {
            ex.printStackTrace();
            return;
        }

        loadFromJson(root);

        leftTermination.setSelected(super.isLeftBorder());
        rightTermination.setSelected(super.isRightBorder());

        this.barTable.getItems().clear();
        getBars().values().forEach(this::addBarToTable);
        this.barTable.refresh();

        this.forceTable.getItems().clear();
        getForces().values().forEach(this::addForceToTable);
        this.forceTable.refresh();

        this.loadTable.getItems().clear();
        getLoads().values().forEach(this::addLoadToTable);
        this.loadTable.refresh();

        this.updateComboBoxes();
    }

    public void loadButtonClicked() throws IOException {
        Utility.openFileDialog(this::loadFromFile);
    }

}
