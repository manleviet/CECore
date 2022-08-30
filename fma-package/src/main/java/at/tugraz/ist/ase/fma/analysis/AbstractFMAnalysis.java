/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.analysis;

import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.cdrmodel.test.ITestCase;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RecursiveTask;

/**
 * Base class for an analysis using a ChocoSolver.
 *
 * @param <T> Type of the analysis result.
 *
 * @author Sebastian Krieter
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 * @author: Tamim Burgstaller (tamim.burgstaller@student.tugraz.at)
 */
@Slf4j
public abstract class AbstractFMAnalysis<T> extends RecursiveTask<T> {

	protected FMDebuggingModel debuggingModel;

	@Getter
	protected ITestCase assumption;

	@Getter
	private boolean timeoutOccurred = false;
	@Getter @Setter
	private long timeout = 1000;

//	@Setter
//	protected IMonitor monitor;

	public AbstractFMAnalysis(@NonNull FMDebuggingModel debuggingModel, @NonNull ITestCase assumption) {
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
}
