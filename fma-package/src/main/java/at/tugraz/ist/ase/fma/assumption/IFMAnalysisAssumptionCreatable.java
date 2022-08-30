/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.assumption;

import at.tugraz.ist.ase.cdrmodel.test.ITestCase;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import lombok.NonNull;

import java.util.List;

public interface IFMAnalysisAssumptionCreatable {
    List<ITestCase> createAssumptions(@NonNull FeatureModel fm);
}
