/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.assumption;

import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.core.FeatureModelException;
import at.tugraz.ist.ase.test.Assignment;
import at.tugraz.ist.ase.test.ITestCase;
import at.tugraz.ist.ase.test.TestCase;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FalseOptionalAssumptions implements IFMAnalysisAssumptionCreatable {
    @Override
    public List<ITestCase> createAssumptions(@NonNull FeatureModel fm) {
        List<ITestCase> testCases = new LinkedList<>();
        for (int i = 1; i < fm.getNumOfFeatures(); i++) {
            Feature feature = fm.getFeature(i);
            if (!fm.isOptionalFeature(feature)) {
                continue;
            }

            ArrayList<Feature> parents = null;
            try {
                parents = new ArrayList<>(fm.getMandatoryParents(feature));
                if (parents.size() < 1) {
                    continue;
                }
            }
            catch (FeatureModelException fme) {
                fme.printStackTrace();
                continue;
            }

            for (Feature parent : parents) {
                String testcase = fm.getFeature(0).getName() + " = true & " + parent.getName() + " = true & "
                        + feature.getName() + " = false";
                List<Assignment> assignments = new LinkedList<>();
                assignments.add(Assignment.builder()
                    .variable(fm.getFeature(0).getName())
                    .value("true")
                    .build());
                assignments.add(Assignment.builder()
                        .variable(parent.getName())
                        .value("true")
                        .build());
                assignments.add(Assignment.builder()
                    .variable(feature.getName())
                    .value("false")
                    .build());

                testCases.add(TestCase.builder()
                    .testcase(testcase)
                    .assignments(assignments).build());
            }
        }

        return testCases;
    }
}
