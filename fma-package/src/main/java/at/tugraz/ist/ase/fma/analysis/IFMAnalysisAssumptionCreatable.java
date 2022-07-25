/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.analysis;

import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.test.TestCase;
import lombok.NonNull;

public interface IFMAnalysisAssumptionCreatable {
    static TestCase createAssumptions(@NonNull FeatureModel fm) {
        return null;
    }
}
