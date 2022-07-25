/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */
package at.tugraz.ist.ase.fma.analysis;

import java.util.concurrent.RecursiveTask;

import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.test.TestCase;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for an analysis using a ChocoSolver.
 *
 * @param <T> Type of the analysis result.
 *
 * @author Sebastian Krieter
 * @author Viet-Man Le
 */
@Slf4j
public abstract class AbstractFMAnalysis<T> extends RecursiveTask<T> implements IFMAnalysisAssumptionCreatable {

	protected FMDebuggingModel debuggingModel;

	@Getter
	protected TestCase assumption;

	@Getter
	private boolean timeoutOccurred = false;
	@Getter @Setter
	private long timeout = 1000;

//	@Setter
//	protected IMonitor monitor;

	public AbstractFMAnalysis(@NonNull FMDebuggingModel debuggingModel, @NonNull TestCase assumption) {
		this.debuggingModel = debuggingModel;
		this.assumption = assumption;
	}

	@Override
	protected T compute() {
//		solver.setTimeout(timeout); // TODO - support timeout
//		if (assumption != null) {
//			solver.assignmentPushAll(assumptions.getLiterals());
//		}
//		assumptions = new LiteralSet(solver.getAssignmentArray());
//		timeoutOccurred = false;
		// TODO - set assumption

//		monitor.checkCancel();
		return analyze();
	}

	protected abstract T analyze();

//	protected final void reportTimeout() throws RuntimeTimeoutException {
//		timeoutOccurred = true;
//		if (throwTimeoutException) {
//			throw new RuntimeTimeoutException();
//		}
//	}

}
