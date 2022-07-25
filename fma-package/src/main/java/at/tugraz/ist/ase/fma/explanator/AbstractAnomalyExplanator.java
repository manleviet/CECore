/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.explanator;

import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.test.TestCase;
import lombok.NonNull;

import java.util.concurrent.RecursiveTask;

public abstract class AbstractAnomalyExplanator<T> extends RecursiveTask<T> {
    protected FMDebuggingModel debuggingModel;

    protected TestCase assumption;

    public AbstractAnomalyExplanator(@NonNull FMDebuggingModel debuggingModel, TestCase assumption) {
        this.debuggingModel = debuggingModel;
        this.assumption = assumption;
    }

    public T compute() {
        return identify();
    }

    public abstract T identify();
}
