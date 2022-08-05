/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 * @author: Tamim Burgstaller (tamim.burgstaller@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma;

/**
 * Describes a feature's anomaly.
 * Has some additional functions, such as mapping its values to bit-values.
 */
public enum AnomalyType {
    NONE(0),
    DEAD(1),
    FULLMANDATORY(1 << 1),
    CONDITIONALLYDEAD(1 << 2),
    FALSEOPTIONAL(1 << 3);

    private final int bitValue;

    AnomalyType(int bitValue) {
        this.bitValue = bitValue;
    }

    public int bitValue() {
        return this.bitValue;
    }

    public AnomalyType getAnomaly(int bitValue) {
        if (bitValue == 0) {
            return NONE;
        }

        for (AnomalyType anomaly : AnomalyType.values()) {
            if ((bitValue & (1 << (anomaly.ordinal() - 1))) > 0) {
                return anomaly;
            }
        }

        return null;
    }
}
