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
import at.tugraz.ist.ase.cdrmodel.test.ITestCase;
import lombok.NonNull;

/**
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 * @author: Tamim Burgstaller (tamim.burgstaller@student.tugraz.at)
 */
public class FullMandatoryAnalysis extends AbstractFMAnalysis<Boolean> {

    public FullMandatoryAnalysis(@NonNull FMDebuggingModel debuggingModel, @NonNull ITestCase assumption) {
        super(debuggingModel, assumption);
    }

    @Override
    protected Boolean analyze() {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(debuggingModel);

        // inconsistent( CF ∪ { c0 } U { fi = false } )
        return checker.isConsistent(debuggingModel.getAllConstraints(), assumption);
    }
}
