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
public class ConditionallyDeadAnalysis extends AbstractFMAnalysis<ITestCase> {
    public ConditionallyDeadAnalysis(@NonNull FMDebuggingModel debuggingModel, @NonNull ITestCase assumption) {
        super(debuggingModel, assumption);
    }

    @Override
    protected Boolean analyze() {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(model);

        // inconsistent( CF ∪ { c0 } U { fj = true } U { fi = true } ) for any fj
        return checker.isConsistent(model.getAllConstraints(), assumption);
    }
}
