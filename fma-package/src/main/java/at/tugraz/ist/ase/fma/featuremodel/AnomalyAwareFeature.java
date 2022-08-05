/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.featuremodel;

import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fma.AnomalyType;
import lombok.NonNull;
import lombok.Setter;

public class AnomalyAwareFeature extends Feature {
    @Setter
    private AnomalyType anomalyType;

    public AnomalyAwareFeature(@NonNull String name, @NonNull String id) {
        super(name, id);

        anomalyType = AnomalyType.NONE;
    }

    public boolean isAnomalyType(AnomalyType anomaly) {
        return anomalyType == anomaly;
    }
}
