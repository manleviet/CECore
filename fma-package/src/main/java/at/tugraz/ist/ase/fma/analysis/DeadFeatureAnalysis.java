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
import at.tugraz.ist.ase.test.ITestCase;
import lombok.NonNull;

/**
 * Analysis checks if a feature is dead.
 */
public class DeadFeatureAnalysis extends AbstractFMAnalysis<Boolean> {

    public DeadFeatureAnalysis(@NonNull FMDebuggingModel debuggingModel, ITestCase assumption) {
        super(debuggingModel, assumption);
    }

    @Override
    protected Boolean analyze() {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(debuggingModel);

        // inconsistent( CF ∪ { c0 } U {fi = true})
        return checker.isConsistent(debuggingModel.getAllConstraints(), assumption);
    }
}
