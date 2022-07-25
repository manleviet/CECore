/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.analysis;

import at.tugraz.ist.ase.cacdr.checker.ChocoConsistencyChecker;
import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.test.Assignment;
import at.tugraz.ist.ase.test.TestCase;
import lombok.NonNull;

import java.util.Collections;

/**
 * Analysis checks if a feature model is void.
 */
public class VoidFMAnalysis extends AbstractFMAnalysis<Boolean> {

    public VoidFMAnalysis(@NonNull FMDebuggingModel debuggingModel, TestCase assumption) {
        super(debuggingModel, assumption);
    }

    @Override
    protected Boolean analyze() {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(debuggingModel);

        // inconsistent( CF ∪ { c0 })
        return checker.isConsistent(debuggingModel.getAllConstraints(), assumption);
    }

    public static TestCase createAssumptions(@NonNull FeatureModel fm) {
        // void feature model - inconsistent(CF ∪ { c0 })
        String testcase = fm.getFeature(0).getName() + " = true";
        Assignment assignment = Assignment.builder()
                .variable(fm.getFeature(0).getName())
                .value("true")
                .build();
        return TestCase.builder()
                .testcase(testcase)
                .assignments(Collections.singletonList(assignment)).build();
    }
}
