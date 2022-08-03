/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.explanator;

import at.tugraz.ist.ase.cacdr.algorithms.hs.HSDAG;
import at.tugraz.ist.ase.cacdr.algorithms.hs.labeler.DirectDebugLabeler;
import at.tugraz.ist.ase.cacdr.algorithms.hs.parameters.DirectDebugParameters;
import at.tugraz.ist.ase.cacdr.checker.ChocoConsistencyChecker;
import at.tugraz.ist.ase.cacdr.eval.CAEvaluator;
import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.test.ITestCase;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FalseOptionalExplanator extends AbstractAnomalyExplanator<List<Set<Constraint>>> {
    public FalseOptionalExplanator(@NonNull FMDebuggingModel debuggingModel, ITestCase assumption) {
        super(debuggingModel, assumption);
    }

    @Override
    public List<Set<Constraint>> identify() {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(debuggingModel);

        Set<ITestCase> TC = new LinkedHashSet<>(Collections.singletonList(assumption));

        // run the hsdag to find diagnoses
        DirectDebugParameters params = DirectDebugParameters.builder()
                .C(debuggingModel.getPossiblyFaultyConstraints())
                .B(debuggingModel.getCorrectConstraints())
                .TV(Collections.emptySet())
                .TC(TC).build();
        DirectDebugLabeler directDebug = new DirectDebugLabeler(checker, params);

        HSDAG hsdag = new HSDAG(directDebug);

//        CAEvaluator.reset();
        hsdag.construct();

        return hsdag.getDiagnoses();
    }
}
