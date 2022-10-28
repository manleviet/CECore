/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2021-2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce;

import at.tugraz.ist.ase.cacdr.checker.ChocoConsistencyChecker;
import at.tugraz.ist.ase.cacdr.eval.CAEvaluator;
import at.tugraz.ist.ase.ce.io.SolutionWriter;
import at.tugraz.ist.ase.ce.translator.ISolutionTranslatable;
import at.tugraz.ist.ase.common.LoggerUtils;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.heuristics.selector.MFVVOValueSelector;
import at.tugraz.ist.ase.heuristics.selector.MFVVOVariableSelector;
import at.tugraz.ist.ase.kb.core.Assignment;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.kb.core.IIntVarKB;
import at.tugraz.ist.ase.kb.core.KB;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.chocosolver.solver.search.strategy.Search.intVarSearch;

@Slf4j
public class Configurator {
    protected final KB kb;
    protected final ISolutionTranslatable translator;

    protected final ConfigurationModel configurationModel;
    protected final ChocoConsistencyChecker checker;

    @Getter
    protected final List<Solution> solutions;

    public Configurator(@NonNull KB kb, boolean rootConstraints, ISolutionTranslatable translator) {
        this.kb = kb;
        this.translator = translator;

        this.configurationModel = new ConfigurationModel(kb,rootConstraints);
        this.configurationModel.initialize(); // unpost all Choco constraints from the Choco model
        this.checker = new ChocoConsistencyChecker(configurationModel);

        solutions = new LinkedList<>();
    }

    public int getNumberSolutions() {
        return solutions.size();
    }

    public Solution getLastSolution() {
        if (solutions.isEmpty())
            return null;
        return solutions.get(getNumberSolutions() - 1);
    }

    private SolutionWriter writer = null;
    protected void find(int maxNumConf, long timeout, Set<Constraint> C, ValueVariableOrdering vvo) {
        // re-add Constraint to the model
        prepareSolver(C);

        // get the solver
        Solver solver = kb.getModelKB().getSolver();
        if (maxNumConf > 0) {
            solver.limitSolution(maxNumConf);
        }
        if (timeout > 0) {
            solver.limitTime(timeout);
        }

        //Add a plugin to print solutions
        AtomicInteger configurationCounter = new AtomicInteger();
//        solver.unplugAllSearchMonitors();
        solver.plugMonitor((IMonitorSolution) () -> {
            configurationCounter.getAndIncrement();

            Solution solution = getCurrentSolution();
            log.trace("{}Found conf {}", LoggerUtils.tab(), configurationCounter.get());
            solutions.add(solution);

            if (writer != null) {
                try {
                    writer.write(solution);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        if (vvo != null) {
            log.trace("{}Add value variable heuristic", LoggerUtils.tab());
            solver.setSearch(intVarSearch(
                    new MFVVOVariableSelector(vvo.getIntVarOrdering()),
                    new MFVVOValueSelector(vvo.getValueOrdering()),
                    // variables to branch on
                    ((IIntVarKB)kb).getIntVars()
            ));
        }

        // solver
        solutions.clear();
        solver.findAllSolutions();

        // remove all constraints
        resetSolver();
    }

    public void findAllSolutions(long timeout) {
        // get the set of constraints
        Set<Constraint> C = configurationModel.getCorrectConstraints();

        find(0, timeout, C, null);
    }

    public void findAllSolutions(long timeout, @NonNull SolutionWriter writer) {
        this.writer = writer;

        findAllSolutions(timeout);
    }

    public void findAllSolutions(@NonNull SolutionWriter writer) {
        this.writer = writer;

        findAllSolutions(0);
    }

    public void findSolutions(int maxNumConf) {
        // get the set of constraints
        Set<Constraint> C = configurationModel.getCorrectConstraints();

        find(maxNumConf, 0, C, null);
    }

    public void findSolutions(int maxNumConf, @NonNull SolutionWriter writer) {
        this.writer = writer;

        findSolutions(maxNumConf);
    }

    public void findSolutions(int maxNumConf, @NonNull Requirement requirement) {
        checkArgument(translator != null, "Translator for the requirement is not set.");

        // translate requirement to Constraint
        Constraint constraint = translator.translate(requirement, kb);
        // get the set of constraints
        Set<Constraint> C = Sets.union(configurationModel.getCorrectConstraints(), Collections.singleton(constraint));

        // re-add Constraint to the model
        find(maxNumConf, 0, C, null);
    }

    public void findSolutions(int maxNumConf, @NonNull Requirement requirement, @NonNull SolutionWriter writer) {
        this.writer = writer;

        findSolutions(maxNumConf, requirement);
    }

    public void findSolutions(int maxNumConf, @NonNull Requirement requirement, @NonNull ValueVariableOrdering vvo) {
        checkArgument(translator != null, "Translator for the requirement is not set.");

        // translate requirement to Constraint
        Constraint constraint = translator.translate(requirement, kb);
        // get the set of constraints
        Set<Constraint> C = Sets.union(configurationModel.getCorrectConstraints(), Collections.singleton(constraint));

        // re-add Constraint to the model
        find(maxNumConf, 0, C, vvo);
    }

    public void findSolutions(int maxNumConf, @NonNull Requirement requirement, @NonNull ValueVariableOrdering vvo, @NonNull SolutionWriter writer) {
        this.writer = writer;

        findSolutions(maxNumConf, requirement, vvo);
    }

    public void findSolutions(int maxNumConf, @NonNull ValueVariableOrdering vvo) {
        // get the set of constraints
        Set<Constraint> C = configurationModel.getCorrectConstraints();

        // re-add Constraint to the model
        find(maxNumConf, 0, C, vvo);
    }

    public void findSolutions(int maxNumConf, @NonNull ValueVariableOrdering vvo, @NonNull SolutionWriter writer) {
        this.writer = writer;

        findSolutions(maxNumConf, vvo);
    }

    public boolean isConsistent(@NonNull Solution solution) {
        checkArgument(translator != null, "Translator for the solution is not set.");

        // translate solution to Constraint
        Constraint constraint = translator.translate(solution, kb);
        Set<Constraint> C = Sets.union(configurationModel.getCorrectConstraints(), Collections.singleton(constraint));

        CAEvaluator.reset();
        return checker.isConsistent(C);
    }

    protected Solution getCurrentSolution() {
        List<Assignment> assignments = kb.getVariableList().stream()
                .map(var -> Assignment.builder()
                        .variable(var.getName())
                        .value(var.getValue())
                        .build())
                .collect(Collectors.toCollection(LinkedList::new));

        return Solution.builder().assignments(assignments).build();
    }

    /**
     * Add constraints to the Choco model before activating the solve() method.
     * @param C the constraints to add to the model.
     */
    protected void prepareSolver(Collection<Constraint> C) {
        Model model = kb.getModelKB();
        C.forEach(c -> c.getChocoConstraints().forEach(model::post));
        log.trace("{}Posted constraints", LoggerUtils.tab());
    }

    /**
     * Remove constraints from the Choco model after solving the model.
     */
    protected void resetSolver() {
        Model model = kb.getModelKB();

        model.getSolver().reset();
        model.unpost(model.getCstrs()); // unpost all constraints

        log.trace("{}Reset model and unpost all constraints", LoggerUtils.tab());
    }
}
