package keelfy.sapr.controller.processing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import keelfy.sapr.controller.IController;
import keelfy.sapr.controller.SaprController;
import keelfy.sapr.dto.AlertDto;
import keelfy.sapr.dto.ChartType;
import keelfy.sapr.dto.ChartValueType;
import keelfy.sapr.dto.Component;
import keelfy.sapr.util.Utility;
import org.apache.commons.math3.util.Precision;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author e.kuzmin
 * */
public class PostProcessorController extends SaprController implements IController {

    private Double selectedStep;
    private int selectedBar = -1;
    private int precision;
    private double lengthSum = 0d;
    private final ObservableList<Component> components = FXCollections.observableArrayList();

    private void addListener(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d\\d*\\.?\\d*")) {
                textField.setText(oldValue);
            }
        });
    }

    @Override
    public void afterLoad() {
        addListener(samplingTextField);
        addListener(pointTextField);
        refresh();
    }

    private void refresh() {
        barComboBox.getItems().clear();
        lengthSum = 0;
        for(int idx = 0; idx < countBars(); idx++){
            barComboBox.getItems().add(idx);
            lengthSum += getBar(idx).getLength();
        }

        precisionComboBox.getItems().clear();
        for(int idx = 0; idx < 18; idx++){
            precisionComboBox.getItems().add(idx);
        }
    }

    private boolean isCorrectValue() {
        if(selectedStep == null || selectedStep == .0) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Некорректные даные")
                    .setContent("Шаг дискретизации не может быть равен 0"));
            return false;
        }
        return true;
    }

    private double getChartDataY(final ChartValueType type, final double x, final int index) {
        switch (type) {
            case Nx:
                return Precision.round(getNxks(index) * x + getNxbs(index), precision);
            case Ux:
                return Precision.round(getUxas(index) * Math.pow(x, 2) + getUxbs(index) * x + getUxcs(index), precision);
            default:
                return Precision.round(getSigmaks(index) * x + getSigmabs(index), precision);
        }
    }

    private LineChart<Number, Number> createLineChart(final ChartValueType type) {
        final var xAxis = new NumberAxis();
        final var yAxis = new NumberAxis();

        final var componentChart = new LineChart<>(xAxis, yAxis);
        final var series = new XYChart.Series<Number, Number>();
        ObservableList<XYChart.Data<Number, Number>> observableData = FXCollections.observableArrayList();

        for (double i = 0; i < getBar(selectedBar).getLength(); i += selectedStep) {
            final var roundedX = Precision.round(i, precision);
            final var roundedY = getChartDataY(type, roundedX, selectedBar);
            final var chartData = new XYChart.Data<Number, Number>(i, roundedY);
            observableData.add(chartData);
        }

        series.setData(observableData);
        componentChart.getData().add(series);
        return componentChart;
    }

    private AreaChart<Number, Number> createAreaChart(final ChartValueType type){
        final var xAxis = new NumberAxis();
        final var yAxis = new NumberAxis();
        final var areaChart = new AreaChart<>(xAxis, yAxis);

        var leftBorder = 0d;
        for(int idx = 0; idx < countBars(); idx++){
            final var bar = getBar(idx);
            final var series = new XYChart.Series<Number, Number>();
            ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();

            for(double x = 0; x < bar.getLength(); x += selectedStep) {
                final var roundedX = Precision.round(x, precision);
                final var roundedY = this.getChartDataY(type, roundedX, idx);
                final var chartData = new XYChart.Data<Number, Number>(roundedX + leftBorder, roundedY);
                data.add(chartData);
            }

            leftBorder += bar.getLength();
            series.setData(data);
            areaChart.getData().add(series);
        }
        return areaChart;
    }

    @FXML
    private TableView<Component> resultTable;

    @FXML
    private TableColumn<Component, Double> xTableColumn;

    @FXML
    private TableColumn<Component, Double> nTableColumn;

    @FXML
    private TableColumn<Component, Double> uTableColumn;

    @FXML
    private TableColumn<Component, Double> sigmaTableColumn;

    @FXML
    private ComboBox<Integer> barComboBox;

    @FXML
    private TextField samplingTextField;

    @FXML
    private ComboBox<Integer> precisionComboBox;

    @FXML
    private TextField pointTextField;

    @FXML
    private Text pointComponentsText;

    public void barSelected() {
        try {
            selectedBar = barComboBox.getValue();
        } catch (Exception ex) {
            selectedBar = -1;
        }
    }

    private double findNByIndex(final double x, final int index) {
        return Precision.round(getNxks(index) * x + getNxbs(index), precision);
    }

    private double findUByIndex(final double x, final int index) {
        return Precision.round(getUxas(index) * Math.pow(x, 2) + getUxbs(index) * x + getUxcs(index), precision);
    }

    private double findSigmaByIndex(final double x, final int index) {
        return Precision.round(getSigmaks(index) * x + getSigmabs(index), precision);
    }

    public void valuesButtonClicked() {
        if (selectedBar < 0) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Некорректные данные")
                    .setContent("Необходимо выбрать стержень!"));
            return; // Проверка указанного стержня
        }

        if (!Utility.isTextFieldNumbersValid(samplingTextField)) {
            return; // Проверка корректных данных в поле ввода
        }

        selectedStep = Utility.parseDouble(samplingTextField);

        if (!isCorrectValue()) {
            return; // Проверка шага дискретизации
        }

        resultTable.getItems().clear();
        components.clear();

        for (double i = 0; i < getBar(selectedBar).getLength(); i += selectedStep) {
            final var roundedX = Precision.round(i, precision);
            final var component = new Component(roundedX,
                    findNByIndex(roundedX, selectedBar),
                    findUByIndex(roundedX, selectedBar),
                    findSigmaByIndex(roundedX, selectedBar));

            components.add(component);
            resultTable.getItems().add(component);
            xTableColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
            nTableColumn.setCellValueFactory(new PropertyValueFactory<>("n"));
            uTableColumn.setCellValueFactory(new PropertyValueFactory<>("u"));
            sigmaTableColumn.setCellValueFactory(new PropertyValueFactory<>("sigma"));
            sigmaTableColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        if (Math.abs(item) > getBar(selectedBar).getSigma()) {
                            setStyle("-fx-background-color: red");
                        } else {
                            setStyle("");
                        }
                        setTextFill(Color.BLACK);
                    }
                }
            });
        }
    }

    public void precisionSelected() {
        precision = precisionComboBox.getValue();
    }

    public void findButtonClicked() {
        if (!Utility.isTextFieldNumbersValid(pointTextField)) {
            return; // Проверка корректности введенных данных
        }

        final var point = Utility.parseDouble(pointTextField);

        if(point > lengthSum) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Некорректные данные")
                    .setContent("Точка вне пределов конструкции"));
            return; // Проверка местоположения точки
        }

        var mark = 0d;
        for (int idx = 0; idx < countBars(); idx++) {
            final var x = point - mark;
            mark += getBar(idx).getLength();
            if (point < mark) {
                pointComponentsText.setText(String.format("N=%f, U=%f, Sigma=%f",
                        findNByIndex(x, idx),
                        findUByIndex(x, idx),
                        findSigmaByIndex(x, idx)));
                break;
            }
        }
    }

    private void onChartAction(ChartType chartType, final Group group, final ComboBox<String> comboBox) {
        if (group.getChildren().size() > 1) {
            group.getChildren().remove(1);
        }
        final var chartValueType = ChartValueType.valueOf(comboBox.getValue());
        final var chart = chartType == ChartType.LINE ? createLineChart(chartValueType) : createAreaChart(chartValueType);
        chart.setLayoutX(0);
        chart.setLayoutY(100);
        chart.setMinHeight(500);
        chart.setMinWidth(960);
        chart.setLegendVisible(false);
        group.getChildren().add(chart);
    }

    private void openChartScene(ChartType chartType) {
        if (selectedBar < 0) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Некорректные данные")
                    .setContent("Необходимо выбрать стержень"));
            return; // Выбран ли стержень
        }

        if (!Utility.isTextFieldNumbersValid(samplingTextField)) {
            return; // Проверка валидности введенных данных
        }

        selectedStep = Utility.parseDouble(samplingTextField);
        final var group = new Group();
        final var componentComboBox = new ComboBox<String>();
        Arrays.stream(ChartValueType.values()).map(ChartValueType::toString).forEach(componentComboBox.getItems()::add);
        componentComboBox.setOnAction(event -> onChartAction(chartType, group, componentComboBox));
        group.getChildren().add(componentComboBox);

        final var stage = new Stage();
        stage.setScene(new Scene(group, 1024, 600));
        stage.setTitle(chartType.getTitle());
        stage.setResizable(true);
        stage.show();
    }

    public void graphicsButtonClicked() {
        openChartScene(ChartType.LINE);
    }

    public void diagramButtonClicked() {
        openChartScene(ChartType.AREA);
    }

    private void saveIntoFile(String fileName) {
        final var file = new File(fileName);
        final var objectMapper = new ObjectMapper();

        final var root = objectMapper.createObjectNode();
        root.put("selected-bar", selectedBar);
        root.put("selected-step", selectedStep);

        final var componentsNode = objectMapper.createArrayNode();
        for (Component component : this.components) {
            final var componentNode = objectMapper.createObjectNode();
            componentNode.put("x", component.getX());
            componentNode.put("n", component.getN());
            componentNode.put("u", component.getU());
            componentNode.put("sigma", component.getSigma());
            componentsNode.add(componentNode);
        }
        root.put("components", componentsNode);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        } catch (IOException ex) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Ошибка при сохранении файла")
                    .setContent(ex.getMessage()));
        }
    }

    public void createFileBClicked() throws IOException {
        Utility.openFileDialog(this::saveIntoFile);
    }

    public void loadFromFile(String fileName) {
        final var file = new File(fileName);

        if (!file.exists()) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Файл не найден")
                    .setContent(file.getAbsolutePath()));
            return;
        }

        final var objectMapper = new ObjectMapper();
        JsonNode root;
        try {
            root = objectMapper.readTree(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        super.loadFromJson(root);

        refresh();
    }

    public void openButtonClicked() throws IOException {
        Utility.openFileDialog(this::loadFromFile);
    }

}
