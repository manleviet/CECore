/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma;

import at.tugraz.ist.ase.fma.analysis.AbstractFMAnalysis;
import at.tugraz.ist.ase.fma.explanator.AbstractAnomalyExplanator;
import at.tugraz.ist.ase.fma.monitor.IMonitor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class FMAnalyzer {
    @Setter
    private IMonitor progressMonitor = null;

    private final Map<AbstractFMAnalysis<Boolean>, AbstractAnomalyExplanator> analyses = new LinkedHashMap<>();

    public FMAnalyzer() {
    }

    public void addAnalysis(AbstractFMAnalysis<Boolean> analysis, AbstractAnomalyExplanator explanator) {
        analyses.put(analysis, explanator);
    }

    public void run() throws ExecutionException, InterruptedException {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        for (AbstractFMAnalysis<Boolean> analysis : analyses.keySet()) {
            pool.execute(analysis);
        }

        List<AbstractAnomalyExplanator> runningTasks = new LinkedList<>();
        for (AbstractFMAnalysis<Boolean> analysis : analyses.keySet()) {
            if (!analysis.get()) {
                AbstractAnomalyExplanator explanator = analyses.get(analysis);
                pool.execute(explanator);

                runningTasks.add(explanator);
            }
        }

        for (AbstractAnomalyExplanator tasks : runningTasks) {
            tasks.join();
        }

        pool.shutdown();
    }
}
