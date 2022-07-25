/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.explanator;

import at.tugraz.ist.ase.cacdr.algorithms.DirectDebug;
import at.tugraz.ist.ase.cacdr.algorithms.FastDiagV3;
import at.tugraz.ist.ase.cacdr.checker.ChocoConsistencyChecker;
import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.test.ITestCase;
import at.tugraz.ist.ase.test.TestCase;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VoidFMExplanator extends AbstractAnomalyExplanator<List<Set<Constraint>>> {

    public VoidFMExplanator(@NonNull FMDebuggingModel debuggingModel, TestCase assumption) {
        super(debuggingModel, assumption);
    }

    public List<Set<Constraint>> identify() {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(debuggingModel);

        DirectDebug directDebug = new DirectDebug(checker);

        Set<ITestCase> TC = new LinkedHashSet(Collections.singletonList(assumption));

        return Collections.singletonList(directDebug.findDiagnosis(debuggingModel.getPossiblyFaultyConstraints(),
                debuggingModel.getCorrectConstraints(), TC));
    }
}
