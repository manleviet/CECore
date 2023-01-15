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
import at.tugraz.ist.ase.ce.translator.ISolutionTranslatable;
import at.tugraz.ist.ase.ce.writer.SolutionWriter;
import at.tugraz.ist.ase.common.LoggerUtils;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.heuristics.selector.MFVVOValueSelector;
import at.tugraz.ist.ase.heuristics.selector.MFVVOVariableSelector;
import at.tugraz.ist.ase.kb.core.*;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.chocosolver.solver.search.strategy.Search.intVarSearch;

@Slf4j
public class Configurator {
    protected final KB kb;

    protected final ConfigurationModel configurationModel;
    protected final ChocoConsistencyChecker checker;

    protected final boolean rootConstraints;

    @Setter
    protected ISolutionTranslatable translator = null;

    @Getter
    protected final List<Solution> solutions = new LinkedList<>();

    @Setter
    protected SolutionWriter writer = null;

    @Getter
    protected Constraint requirement = null;

    protected AbstractStrategy<?> defaultSearch;
    protected Model model;
    protected Solver solver;

    public Configurator(@NonNull KB kb, boolean rootConstraints) {
        this.kb = kb;
        this.rootConstraints = rootConstraints;

        this.configurationModel = new ConfigurationModel(kb, rootConstraints);
        this.configurationModel.initialize(); // unpost all Choco constraints from the Choco model
        this.checker = new ChocoConsistencyChecker(configurationModel);

        this.defaultSearch = Search.defaultSearch(kb.getModelKB());
        this.model = kb.getModelKB();
        this.solver = this.model.getSolver();
    }

    public Configurator(@NonNull KB kb, boolean rootConstraints, ISolutionTranslatable translator) {
        this(kb, rootConstraints);

        this.translator = translator;
    }

    public Configurator(@NonNull KB kb, boolean rootConstraints, ISolutionTranslatable translator, SolutionWriter writer) {
        this(kb, rootConstraints, translator);

        this.writer = writer;
    }

    public int getNumberSolutions() {
        return solutions.size();
    }

    public Solution getLastestSolution() {
        if (solutions.isEmpty())
            return null;
        return solutions.get(getNumberSolutions() - 1);
    }

    public void emptySolutions() {
        solutions.clear();
    }

    /* Control mode */

    public void initializeWithKB() {
        // get the set of constraints
        Set<Constraint> C = configurationModel.getCorrectConstraints();

        prepareSolver(C);
    }

    public void initializeWithNotKB() {
        model.post(kb.getNotKB().getChocoConstraints().toArray(new org.chocosolver.solver.constraints.Constraint[0]));
        log.trace("{}Posted constraints", LoggerUtils.tab());
    }

    /**
     * Add constraints to the Choco model before activating the solve() method.
     * @param C the constraints to add to the model.
     */
    protected void prepareSolver(Collection<Constraint> C) {
        C.forEach(c -> c.getChocoConstraints().forEach(model::post));
        log.trace("{}Posted constraints", LoggerUtils.tab());
    }

    public void setRequirement(@NonNull Requirement req) {
        checkArgument(translator != null, "Translator for the requirement is not set.");

        // translate requirement to Constraint
        requirement = translator.translate(req, kb);

        requirement.getChocoConstraints().forEach(model::post);
        log.trace("{}Posted requirement", LoggerUtils.tab());
    }

    public void removeRequirement() {
        if (requirement != null) {
            model.unpost(requirement.getChocoConstraints().toArray(new org.chocosolver.solver.constraints.Constraint[0]));
            log.trace("{}Unposted requirement", LoggerUtils.tab());
        }
    }

    public void setVVO(@NonNull ValueVariableOrdering vvo) {
        log.trace("{}Add value variable heuristic", LoggerUtils.tab());
        IntVar[] vars = kb.getVariableList().stream().map(v -> v instanceof IntVariable ? ((IntVariable) v).getChocoVar() : ((BoolVariable) v).getChocoVar()).toArray(IntVar[]::new);

        solver.setSearch(intVarSearch(
                new MFVVOVariableSelector(vvo.getIntVarOrdering()),
                new MFVVOValueSelector(vvo.getValueOrdering()),
                // variables to branch on
                vars
        ));
    }

    public void clearVVO() {
        log.trace("{}Clear value variable heuristic", LoggerUtils.tab());

        solver.setSearch(defaultSearch);
    }

    public boolean find(int maxNumConf, long timeout) {
        // get the solver
        if (timeout > 0) {
            solver.limitTime(timeout);
            log.trace("{}Set timeout: {} ms", LoggerUtils.tab(), timeout);
        }

        // solver
        int numSolve = 0; // number of solve calls
        int numSolutions = 0;
        int numSimilar = 0;
        boolean could_more_solution = true;
        do {

            if (!solver.solve()) {
                could_more_solution = false;
            } else {
                numSolve++;
                Solution solution = getCurrentSolution();

                if (contains(solution)) {
                    numSimilar++;
                } else {
                    numSolutions++;
                    log.trace("{}{}. {}", LoggerUtils.tab(), numSolutions, solution);
                    solutions.add(solution);

                    if (writer != null) {
                        try {
                            writer.write(solution);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } while (could_more_solution && (maxNumConf == 0 || numSolutions < maxNumConf));
        log.trace("{}Solve executed {} times", LoggerUtils.tab(), numSolve);
        log.trace("{}Found {} similar solutions", LoggerUtils.tab(), numSimilar);

        return could_more_solution;
    }

    /**
     * Remove constraints from the Choco model after solving the model.
     */
    public void reset() {
        solver.hardReset();
        model.unpost(model.getCstrs()); // unpost all constraints

        log.trace("{}Reset model and unpost all constraints", LoggerUtils.tab());
    }

    /* Compact mode */

    public void findAllSolutions(boolean notKB, long timeout) {
        prefindSolutions(notKB, null, null);

        find(0, timeout);

        // remove all constraints
        reset();
    }

    private void prefindSolutions(boolean notKB, Requirement requirement, ValueVariableOrdering vvo) {
        emptySolutions();
        if (notKB) {
            initializeWithNotKB();
        } else {
            initializeWithKB();
        }

        if (requirement != null) {
            setRequirement(requirement);
        }
        if (vvo != null) {
            setVVO(vvo);
        }
    }

    public void findAllSolutions(boolean notKB, long timeout, @NonNull SolutionWriter writer) {
        setWriter(writer);

        findAllSolutions(notKB, timeout);
    }

    public void findAllSolutions(boolean notKB, @NonNull SolutionWriter writer) {
        setWriter(writer);

        findAllSolutions(notKB,0);
    }

    public void findSolutions(boolean notKB, int maxNumConf) {
        prefindSolutions(notKB, null, null);

        find(maxNumConf, 0);

        // remove all constraints
        reset();
    }

    public void findSolutions(boolean notKB, int maxNumConf, @NonNull SolutionWriter writer) {
        setWriter(writer);

        findSolutions(notKB, maxNumConf);
    }

    // TODO - generic method
    public void findSolutions(boolean notKB, int maxNumConf, @NonNull Requirement requirement) {
        checkArgument(translator != null, "Translator for the requirement is not set.");

        prefindSolutions(notKB, requirement, null);

        // re-add Constraint to the model
        find(maxNumConf, 0);

        // remove all constraints
        reset();
    }

    // TODO - generic method
    public void findSolutions(boolean notKB, int maxNumConf, @NonNull Requirement requirement, @NonNull SolutionWriter writer) {
        setWriter(writer);

        findSolutions(notKB, maxNumConf, requirement);
    }

    // TODO - generic method
    public void findSolutions(boolean notKB, int maxNumConf, @NonNull Requirement requirement, @NonNull ValueVariableOrdering vvo) {
        checkArgument(translator != null, "Translator for the requirement is not set.");

        prefindSolutions(notKB, requirement, vvo);

        // re-add Constraint to the model
        find(maxNumConf, 0);

        clearVVO();

        // remove all constraints
        reset();
    }

    public void findSolutions(boolean notKB, int maxNumConf, @NonNull ValueVariableOrdering vvo) {
        prefindSolutions(notKB, null, vvo);

        // re-add Constraint to the model
        find(maxNumConf, 0);

        clearVVO();

        // remove all constraints
        reset();
    }

    public void findSolutions(boolean notKB, int maxNumConf, @NonNull ValueVariableOrdering vvo, @NonNull SolutionWriter writer) {
        setWriter(writer);

        findSolutions(notKB, maxNumConf, vvo);
    }

    public <T extends Solution> boolean isConsistent(@NonNull T t) {
        checkArgument(translator != null, "Translator for the solution is not set.");

        // translate solution to Constraint
        Constraint constraint = translator.translate(t, kb);
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

    // TODO: migrate to common-package and generic method - T needs to have equals and hashCode methods
    private boolean contains(Solution solution) {
        return solutions.stream().anyMatch(s -> s.equals(solution));
    }
}
