/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce.translator.fm;

import at.tugraz.ist.ase.ce.Solution;
import at.tugraz.ist.ase.ce.translator.ISolutionTranslatable;
import at.tugraz.ist.ase.common.LoggerUtils;
import at.tugraz.ist.ase.kb.core.Assignment;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.kb.core.KB;
import at.tugraz.ist.ase.kb.core.translator.fm.FMAssignmentsTranslator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FMSolutionTranslator implements ISolutionTranslatable {

    protected FMAssignmentsTranslator translator = new FMAssignmentsTranslator();

    /**
     * Translates an FM solution to Constraint
     */
    @Override
    public Constraint translate(@NonNull Solution solution, @NonNull KB kb) {
        log.trace("{}Translating solution [solution={}] >>>", LoggerUtils.tab(), solution);
        Constraint constraint = new Constraint(solution.toString());

        translator.translate(solution.getAssignments(), kb,
                constraint.getChocoConstraints(), constraint.getNegChocoConstraints());

        // copy the generated constraints to Solution
        constraint.getChocoConstraints().forEach(solution::addChocoConstraint);
        constraint.getNegChocoConstraints().forEach(solution::addNegChocoConstraint);

        // remove the translated constraints from the Choco model
        // TODO - should move out to the configurator class
//        kb.getModelKB().unpost(kb.getModelKB().getCstrs());

        log.debug("{}Translated solution [solution={}] >>>", LoggerUtils.tab(), solution);
        return constraint;
    }

    /**
     * Translates an FM solution to a list of Constraints
     */
    @Override
    public List<Constraint> translateToList(@NonNull Solution solution, @NonNull KB kb) {
        log.trace("{}Translating solution [solution={}] >>>", LoggerUtils.tab(), solution);
        List<Constraint> constraints = new LinkedList<>();

        for (Assignment assign: solution.getAssignments()) {
            Constraint constraint = new Constraint(assign.toString());

            translator.translate(assign, kb,
                    constraint.getChocoConstraints(), constraint.getNegChocoConstraints());

            // copy the generated constraints to Solution
            constraint.getChocoConstraints().forEach(solution::addChocoConstraint);
            constraint.getNegChocoConstraints().forEach(solution::addNegChocoConstraint);

            constraints.add(constraint);
        }

        // remove the translated constraints from the Choco model
        // TODO - should move out to the configurator class
//        kb.getModelKB().unpost(kb.getModelKB().getCstrs());

        log.debug("{}Translated solution [solution={}] >>>", LoggerUtils.tab(), solution);
        return constraints;
    }

//    private boolean isCorrectAssignment(String varName, IntVar var, String value, int chocoValue) {
//        return var != null && (!value.equals("NULL"))
//                && (chocoValue != -1);
//    }
}
