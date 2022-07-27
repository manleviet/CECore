/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.analysis;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import at.tugraz.ist.ase.cacdr.checker.ChocoConsistencyChecker;
import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.test.Assignment;
import at.tugraz.ist.ase.test.ITestCase;
import at.tugraz.ist.ase.test.TestCase;
import lombok.NonNull;

/**
 * Analysis checks if a feature is dead.
 */
public class DeadFeatureAnalysis extends AbstractFMAnalysis<Boolean> implements IFMAnalysisAssumptionCreatable {

    public DeadFeatureAnalysis(@NonNull FMDebuggingModel debuggingModel, ITestCase assumption) {
        super(debuggingModel, assumption);
    }

    @Override
    protected Boolean analyze() {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(debuggingModel);

        // inconsistent( CF ∪ { c0 } U {fi = true})
        return checker.isConsistent(debuggingModel.getAllConstraints(), assumption);
    }

    public static List<ITestCase> createAssumptions(@NonNull FeatureModel fm) {
        // dead feature - inconsistent(CF ∪ { c0 } U {fi = true})
        List<ITestCase> testCases = new LinkedList<>();
        for (int i = 1; i < fm.getNumOfFeatures(); i++) {
            Feature feature = fm.getFeature(i);

            String testcase = fm.getFeature(0).getName() + " = true & " + feature.getName() + " = true";
            List<Assignment> assignments = new LinkedList<>();
            assignments.add(Assignment.builder()
                    .variable(fm.getFeature(0).getName())
                    .value("true")
                    .build());
            assignments.add(Assignment.builder()
                    .variable(feature.getName())
                    .value("true")
                    .build());

            testCases.add(TestCase.builder()
                    .testcase(testcase)
                    .assignments(assignments).build());
        }
        return testCases;
    }
}
