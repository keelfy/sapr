package keelfy.sapr.dto;

import lombok.Getter;

/**
 * @author e.kuzmin
 */
@Getter
public enum ChartType {

    LINE,
    AREA;

    private final String title;

    ChartType() {
        final var lowerCase = this.toString().toLowerCase().replaceAll("_", " ");
        this.title = lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1) + " chart";
    }

}
