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
public class Force {

    /**
     * Идентификатор
     * */
    private int id;

    /**
     * Значение
     * */
    private double value;

}
