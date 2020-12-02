package keelfy.sapr.controller.processing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import keelfy.sapr.controller.IController;
import keelfy.sapr.controller.SaprController;
import keelfy.sapr.dto.AlertDto;
import keelfy.sapr.dto.Bar;
import keelfy.sapr.dto.Force;
import keelfy.sapr.dto.Load;
import keelfy.sapr.util.CalculationUtil;
import keelfy.sapr.util.Utility;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.util.Precision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author e.kuzmin
 * */
public class ProcessorController extends SaprController implements IController {

    private int nodeCount;

    private final ArrayList<Double> elasticities = new ArrayList<>();
    private final ArrayList<Double> areas = new ArrayList<>();
    private final ArrayList<Double> lengths = new ArrayList<>();

    @FXML
    private TextField fileNameTextField;

    @FXML
    private Button openButton;

    public void calculate() {
        super.getNxks().clear();
        super.getNxbs().clear();
        super.getUxas().clear();
        super.getUxbs().clear();
        super.getUxcs().clear();
        super.getSigmabs().clear();
        super.getSigmaks().clear();

        final var reactionMatrixData = new double[nodeCount][nodeCount];
        final int barCount = nodeCount - 1;

        double[] forces = new double[nodeCount];
        double[] loads = new double[barCount];

        for (int idx = 0; idx < nodeCount; idx++) {
            forces[idx] = .0;
            for (Force force : getForces().values()) {
                if (force.getId() == idx) {
                    forces[idx] = force.getValue();
                }
            }
        }

        for (int idx = 0; idx < barCount; idx++) {
            loads[idx] = .0;
            for (Load load : getLoads().values()) {
                if (load.getId() == idx) {
                    loads[idx] = load.getValue();
                }
            }
        }

        for(int i = 0; i < nodeCount; i++){
            for(int j = 0; j < nodeCount; j++){
                if(i == j && i > 0 && j > 0 && i < barCount && j < barCount) {
                    reactionMatrixData[i][j] = (elasticities.get(i - 1) * areas.get(i - 1)) / lengths.get(i - 1) + (elasticities.get(j) * areas.get(j)) / lengths.get(j);
                } else if(i == j + 1) {
                    reactionMatrixData[i][j] = -(elasticities.get(j) * areas.get(j)) / lengths.get(j);
                } else if(j == i + 1) {
                    reactionMatrixData[i][j] = -(elasticities.get(i) * areas.get(i)) / lengths.get(i);
                } else {
                    reactionMatrixData[i][j] = .0;
                }
            }
        }

        double[][] reactionVectorData = new double[nodeCount][1];

        for(int idx = 1; idx < barCount; idx++) {
            reactionVectorData[idx][0] = forces[idx] + loads[idx] * lengths.get(idx) / 2 + loads[idx - 1] * lengths.get(idx - 1) / 2;
        }

        if(super.isLeftBorder()) {
            reactionMatrixData[0][0] = 1.0;
            reactionMatrixData[0][1] = .0;
            reactionMatrixData[1][0] = .0;
            reactionVectorData[0][0] = .0;
        } else {
            reactionMatrixData[0][0] = (elasticities.get(0) * areas.get(0)) / lengths.get(0);
            reactionVectorData[0][0] = forces[0] + loads[0] * lengths.get(0) / 2;
        }

        if(super.isRightBorder()) {
            reactionMatrixData[barCount][barCount] = 1.0;
            reactionMatrixData[barCount - 1][barCount] = .0;
            reactionMatrixData[barCount][barCount - 1] = .0;
            reactionVectorData[barCount][0] = .0;
        } else {
            reactionMatrixData[barCount][barCount] = (elasticities.get(barCount - 1) * areas.get(barCount - 1)) / lengths.get(barCount - 1);
            reactionVectorData[barCount][0] = forces[barCount] + loads[barCount - 1] * lengths.get(barCount - 1) / 2;
        }

        final var reactionMatrix = MatrixUtils.createRealMatrix(reactionMatrixData);
        final var reactionVector = MatrixUtils.createRealMatrix(reactionVectorData);
        final var inverseReactionMatrix = new LUDecomposition(reactionMatrix).getSolver().getInverse();
        final var deltaVector = inverseReactionMatrix.multiply(reactionVector);

        final var uZeros = new double[barCount];
        final var uLengths = new double[barCount];
        for(int idx = 0; idx < barCount; idx++) {
            uZeros[idx] = deltaVector.getEntry(idx, 0);
        }
        System.arraycopy(uZeros, 1, uLengths, 0, barCount - 1);
        uLengths[barCount - 1] = deltaVector.getEntry(barCount, 0);

        for(int idx = 0; idx < barCount; idx++) {
            final var elasticity = elasticities.get(idx);
            final var area = areas.get(idx);
            final var length = lengths.get(idx);

            final var nxk = -loads[idx];
            getNxks().add(nxk);

            final var nxb = CalculationUtil.calculateNxb(elasticity, area, length, uZeros[idx], uLengths[idx], loads[idx]);
            getNxbs().add(nxb);

            final var uxa = CalculationUtil.calculateUxa(elasticity, area, loads[idx]);
            getUxas().add(uxa);

            final var uxb = CalculationUtil.calculateUxb(elasticity, area, length, uZeros[idx], uLengths[idx], loads[idx]);
            getUxbs().add(uxb);

            final var uxc = uZeros[idx];
            getUxcs().add(uxc);

            final var sigmak = getNxks().get(idx) / areas.get(idx);
            getSigmaks().add(sigmak);

            final var sigmab = getNxbs().get(idx) / areas.get(idx);
            getSigmabs().add(sigmab);
        }

        for(int idx = 0; idx < barCount; idx++) {

            final var elasticity = elasticities.get(idx);
            final var area = areas.get(idx);
            final var length = lengths.get(idx);

            final var nxb = CalculationUtil.calculateNxb(elasticity, area, length, uZeros[idx], uLengths[idx], loads[idx]);
            final var uxa = CalculationUtil.calculateUxa(elasticity, area, loads[idx]);
            final var uxb = CalculationUtil.calculateUxb(elasticity, area, length, uZeros[idx], uLengths[idx], loads[idx]);

            System.out.println("N" + (idx+1) + "x: " + Precision.round(-loads[idx], 4) + "x + " + Precision.round(nxb, 4));
            System.out.println("U" + (idx+1) + "x: " + uxa + "x^2 + " + uxb + "x + " + uZeros[idx]);
            System.out.println("Sigma" +  (idx+1) + "x: " + Precision.round(-loads[idx] / areas.get(idx), 4) + "x + " +Precision.round(nxb / areas.get(idx), 4) + "\n");
        }
    }

    public void calculateButtonClicked() {
        final var fileName = fileNameTextField.getText() + ".json";
        final var file = new File(fileName);

        if (!file.exists()) {
            Utility.showAlert(new AlertDto()
                    .setHeader("Файл не найден")
                    .setContent(file.getAbsolutePath()));
            return;
        }

        elasticities.clear();
        areas.clear();
        lengths.clear();

        final var objectMapper = new ObjectMapper();
        JsonNode root;
        try {
            root = objectMapper.readTree(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        super.loadFromJson(root);

        System.out.println(super.countBars() + " " + super.countForces() + " " + super.countLoads());

        for (Bar bar : getBars().values()) {
            elasticities.add(bar.getElasticity());
            areas.add(bar.getArea());
            lengths.add(bar.getLength());
        }

        nodeCount = elasticities.size() + 1;

        calculate();

        Utility.showAlert(new AlertDto()
                .setAlertType(Alert.AlertType.INFORMATION)
                .setTitle("Успех!")
                .setHeader("Расчёт проведен")
                .setContent("Результаты сохранены в файл и выведены в консоль"));

        final var stage = (Stage) openButton.getScene().getWindow();
        stage.close();

        root = convertIntoJsonNode(objectMapper);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
