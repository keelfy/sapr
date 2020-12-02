package keelfy.sapr.util;

import lombok.experimental.UtilityClass;

/**
 * @author e.kuzmin
 */
@UtilityClass
public class CalculationUtil {

    public double calculateNxb(double E, double A, double L, double Up0, double UpL, double q){
        return (E * A / L) * (UpL - Up0) + q * L / 2;
    }

    public double calculateUxb(double E, double A, double L, double Up0, double UpL, double q){
        return (UpL - Up0 + (q * L * L) / (2 * E * A)) / L;
    }

    public double calculateUxa(double E, double A, double q) {
        return -q / (2 * E * A);
    }

}
