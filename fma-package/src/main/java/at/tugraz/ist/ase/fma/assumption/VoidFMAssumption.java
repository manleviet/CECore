/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.assumption;

import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.test.Assignment;
import at.tugraz.ist.ase.test.ITestCase;
import at.tugraz.ist.ase.test.TestCase;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

public class VoidFMAssumption implements IFMAnalysisAssumptionCreatable {
    public List<ITestCase> createAssumptions(@NonNull FeatureModel fm) {
        // void feature model - inconsistent(CF ∪ { c0 })
        String testcase = fm.getFeature(0).getName() + " = true";
        Assignment assignment = Assignment.builder()
                .variable(fm.getFeature(0).getName())
                .value("true")
                .build();
        return Collections.singletonList(TestCase.builder()
                .testcase(testcase)
                .assignments(Collections.singletonList(assignment)).build());
    }
}
