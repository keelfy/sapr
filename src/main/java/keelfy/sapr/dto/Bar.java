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
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
public class Bar {

    /**
     * Идентификатор
     * */
    private int id;

    private double area;

    private double elasticity;

    private double length;

    private double sigma;

}
