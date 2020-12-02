package keelfy.sapr.dto;

import javafx.scene.control.Alert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author e.kuzmin
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AlertDto {

    private String title = "Внимание!";

    private String header;

    private String content;

    private Alert.AlertType alertType = Alert.AlertType.WARNING;

}
