package keelfy.sapr.service;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import keelfy.sapr.dto.Bar;
import keelfy.sapr.dto.Force;
import keelfy.sapr.dto.Load;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * @author e.kuzmin
 */
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "create")
public class Painter {

    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color BAR_COLOR = Color.BLACK;
    private static final Color BORDER_COLOR = BAR_COLOR;
    private static final Color FORCE_COLOR = Color.BLUE;
    private static final Color LOAD_COLOR = Color.GREEN;

    private static final double WHOLE_LENGTH = 900;
    private static final double WHOLE_AREA = 450;
    private static final double AXIS_POSITION_Y = 300;
    private static final double LEFT_X = 62;
    private static final double RIGHT_X = 962;

    private final List<Double> paintLengthArray = new ArrayList<>();
    private final List<Double> paintAreaArray = new ArrayList<>();
    private final List<Rectangle> barRectanglesArray = new ArrayList<>();
    private final Map<Integer, Double> barLengths = new HashMap<>();
    private final Map<Integer, Double> barAreas = new HashMap<>();

    private final Canvas canvas = new Canvas(1024, 680);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final Group group = new Group(canvas);

    private final Map<Integer, Bar> bars;
    private final Map<Integer, Force> forces;
    private final Map<Integer, Load> loads;

    @Setter
    private boolean enableLeftBorder = false;

    @Setter
    private boolean enableRightBorder = false;

    private double totalLength = 0;
    private double totalArea = 0;

    public void draw() {

        calculateScaling();

        prepareGraphics();

        drawBars();

        drawBorders();

        drawForces();

        drawLoads();

        show();

    }

    private void calculateScaling() {

        for (final Bar bar : bars.values()) {
            totalLength += bar.getLength();
            totalArea += bar.getArea();
        }

        double newTotalLength = 0;
        double newTotalArea = 0;

        for(int idx = 0; idx < bars.size(); idx++) {
            barLengths.put(idx, Math.max(bars.get(idx).getLength(), 0.1 * totalLength));
            newTotalLength += barLengths.get(idx);
        }

        for(int idx = 0; idx < bars.size(); idx++){
            barAreas.put(idx, Math.max(bars.get(idx).getArea(), 0.1 * totalArea));
            newTotalArea += barAreas.get(idx);
        }

        for (int idx = 0; idx < bars.size(); idx++) {
            paintLengthArray.add((barLengths.get(idx) / newTotalLength) * WHOLE_LENGTH);
            paintAreaArray.add((barAreas.get(idx) / newTotalArea) * WHOLE_AREA);
        }
    }

    private void prepareGraphics() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(BACKGROUND_COLOR);
    }

    private void drawBars() {

        double x = LEFT_X;
        double y;

        for (int idx = 0; idx < bars.size(); idx++) {
            final var barRectangle = new Rectangle();
            barRectangle.setX(x);
            y = AXIS_POSITION_Y - 0.5 * paintAreaArray.get(idx);
            barRectangle.setY(y);
            barRectangle.setHeight(paintAreaArray.get(idx));
            barRectangle.setWidth(paintLengthArray.get(idx));
            barRectangle.setFill(Color.TRANSPARENT);
            barRectangle.setStroke(BAR_COLOR);
            x += paintLengthArray.get(idx);
            barRectanglesArray.add(barRectangle);
            group.getChildren().add(barRectangle);
        }
    }

    private void drawBorder(double x, int paintAreaIndex, double offsetMultiplier) {
        final var line = new Line();
        line.setStartX(x);
        line.setEndX(x);
        line.setStartY((AXIS_POSITION_Y - 0.5 * paintAreaArray.get(paintAreaIndex)) - 25);
        line.setEndY((AXIS_POSITION_Y + 0.5 * paintAreaArray.get(paintAreaIndex)) + 25);
        line.setStrokeWidth(2);
        line.setStroke(BORDER_COLOR);
        group.getChildren().add(line);

        double offset = (line.getEndY() - line.getStartY()) / 10;
        for (int idx = 0; idx <= 10; idx++) {
            final var streak = new Line();
            streak.setStartY(line.getStartY() + idx * offset);
            streak.setStartX(line.getStartX());
            streak.setEndX(line.getEndX() - offsetMultiplier * offset);
            streak.setEndY(streak.getStartY() + offsetMultiplier * offset);
            streak.setStrokeWidth(2);
            streak.setStroke(BORDER_COLOR);
            group.getChildren().add(streak);
        }
    }

    private void drawBorders() {
        if (enableLeftBorder) {
            drawBorder(LEFT_X, 0, 1);
        }

        if (enableRightBorder) {
            drawBorder(RIGHT_X, paintAreaArray.size() - 1, -1);
        }
    }

    private void drawForces() {
        double forceLineLength = 30;

        for (final Force force : this.forces.values()) {
            if (force.getValue() == 0) {
                continue;
            }

            final var barRectangles = barRectanglesArray.get(force.getId());

            final var forceLine = new Line();
            forceLine.setStartX(barRectangles.getX() - (force.getValue() < 0 ? forceLineLength : 0));
            forceLine.setStartY(AXIS_POSITION_Y);
            forceLine.setEndY(AXIS_POSITION_Y);
            forceLine.setStrokeWidth(3);
            forceLine.setStroke(FORCE_COLOR);
            forceLine.setEndX(forceLine.getStartX() + forceLineLength);

            final var stroke1 = new Line();
            stroke1.setStartX(force.getValue() > 0 ? forceLine.getEndX() : forceLine.getStartX());
            stroke1.setStartY(forceLine.getEndY());
            stroke1.setEndX(stroke1.getStartX()  + forceLineLength / 2 * (force.getValue() > 0 ? -1 : 1));
            stroke1.setEndY(forceLine.getEndY() - forceLineLength / 2);
            stroke1.setStrokeWidth(2);
            stroke1.setStroke(FORCE_COLOR);
            group.getChildren().add(stroke1);

            final var stroke2 = new Line();
            stroke2.setStartX(force.getValue() > 0 ? forceLine.getEndX() : forceLine.getStartX());
            stroke2.setStartY(forceLine.getEndY());
            stroke2.setEndX(stroke2.getStartX() - forceLineLength / 2 * (force.getValue() < 0 ? -1 : 1));
            stroke2.setEndY(forceLine.getEndY() + forceLineLength / 2);
            stroke2.setStrokeWidth(2);
            stroke2.setStroke(FORCE_COLOR);
            group.getChildren().add(stroke2);

            group.getChildren().add(forceLine);
        }
    }

    private void drawLoads() {
        double offset;
        for (final Load load : loads.values()) {
            if (load.getValue() == 0) {
                continue;
            }

            final var barRectangles = barRectanglesArray.get(load.getId());
            final var axisLine = new Line();

            axisLine.setStartX(barRectangles.getX());
            axisLine.setStartY(AXIS_POSITION_Y);
            axisLine.setEndY(AXIS_POSITION_Y);
            axisLine.setEndX(barRectangles.getX() + barRectangles.getWidth());
            axisLine.setStrokeWidth(1);
            axisLine.setStroke(LOAD_COLOR);
            offset = (barRectangles.getWidth() / 10);

            for (int idx = 1; idx < 11; idx++) {
                for (int i = 0; i < 2; i++) {
                    final var stroke = new Line();
                    stroke.setStartX(barRectangles.getX() + idx * offset - (idx == 10 && load.getValue() < 0 ? barRectangles.getWidth() : 0));
                    stroke.setStartY(AXIS_POSITION_Y);
                    stroke.setEndX(stroke.getStartX() - 8 * (load.getValue() < 0 ? -1 : 1));
                    stroke.setEndY(stroke.getStartY() - 8 * (i == 1 ? -1 : 1));
                    stroke.setStrokeWidth(1);
                    stroke.setStroke(LOAD_COLOR);
                    group.getChildren().add(stroke);
                }
            }
            group.getChildren().add(axisLine);
        }
    }

    private void show() {
        final var stage = new Stage();
        stage.setScene(new Scene(group, canvas.getWidth(), canvas.getHeight()));
        stage.setResizable(false);
        stage.show();
    }

}
