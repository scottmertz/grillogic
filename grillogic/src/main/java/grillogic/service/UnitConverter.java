package grillogic.service;

import grillogic.model.Unit;
import org.springframework.stereotype.Service;

@Service
public class UnitConverter {
    // How many of the base unit equals 1 of this unit
    private double toBaseFactor(Unit unit) {
        switch (unit) {
            case G:       return 1.0;
            case KG:      return 1000.0;
            case OZ:      return 28.3495;
            case LB:      return 453.592;
            case ML:      return 1.0;
            case L:       return 1000.0;
            case FL_OZ:   return 29.5735;
            case CUP:     return 236.588;
            case TSP:     return 4.92892;
            case TBSP:    return 14.7868;
            case EACH:    return 1.0;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    private boolean isWeight(Unit unit) {
        return unit == Unit.G || unit == Unit.KG || unit == Unit.OZ || unit == Unit.LB;
    }

    private boolean isVolume(Unit unit) {
        return unit == Unit.ML || unit == Unit.L || unit == Unit.FL_OZ
                || unit == Unit.CUP || unit == Unit.TSP || unit == Unit.TBSP;
    }

    public double convert(double amount, Unit from, Unit to) {
        if (from == to) {
            return amount;
        }

        if (from == Unit.EACH || to == Unit.EACH) {
            throw new IllegalArgumentException(
                    "Cannot convert EACH to/from a measured unit - " + from + " to " + to
            );
        }

        boolean sameCategory = (isWeight(from) && isWeight(to)) || (isVolume(from) && isVolume(to));
        if (!sameCategory) {
            throw new IllegalArgumentException(
                    "Cannot convert across categories (weight vs volume): " + from + " to " + to
            );
        }

        double amountInBase = amount * toBaseFactor(from);
        return amountInBase / toBaseFactor(to);
    }
}
