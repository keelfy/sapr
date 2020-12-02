package keelfy.sapr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author e.kuzmin
 * */
@Data
@ToString
@AllArgsConstructor
public class Component {

    private double x;

    private double n;

    private double u;

    private double sigma;

}
