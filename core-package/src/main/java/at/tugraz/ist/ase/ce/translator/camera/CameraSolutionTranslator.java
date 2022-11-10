/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce.translator.camera;

import at.tugraz.ist.ase.ce.Solution;
import at.tugraz.ist.ase.ce.translator.ISolutionTranslatable;
import at.tugraz.ist.ase.common.LoggerUtils;
import at.tugraz.ist.ase.kb.camera.CameraKB;
import at.tugraz.ist.ase.kb.core.Assignment;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.kb.core.KB;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class CameraSolutionTranslator implements ISolutionTranslatable {

    protected CameraAssignmentsTranslator translator = new CameraAssignmentsTranslator();

    /**
     * Translates a Camera solution to Constraint
     */
    @Override
    public Constraint translate(@NonNull Solution solution, @NonNull KB kb) {
        // check if the KB is a CameraKB
        checkArgument(kb instanceof CameraKB, "The KB must be a CameraKB");
        CameraKB cameraKB = (CameraKB) kb;

        log.trace("{}Translating solution [solution={}] >>>", LoggerUtils.tab(), solution);
        Constraint constraint = new Constraint(solution.toString());

        translator.translate(solution.getAssignments(), cameraKB,
                constraint.getChocoConstraints(), constraint.getNegChocoConstraints());

        // copy the generated constraints to Solution
        constraint.getChocoConstraints().forEach(solution::addChocoConstraint);
        constraint.getNegChocoConstraints().forEach(solution::addNegChocoConstraint);

        // remove the translated constraints from the Choco model
        // TODO - should move out to the configurator class
        cameraKB.getModelKB().unpost(cameraKB.getModelKB().getCstrs());

        log.debug("{}Translated solution [solution={}] >>>", LoggerUtils.tab(), solution);
        return constraint;
    }

    /**
     * Translates a Camera solution to a list of Constraints
     */
    @Override
    public List<Constraint> translateToList(@NonNull Solution solution, @NonNull KB kb) {
        // check if the KB is a CameraKB
        checkArgument(kb instanceof CameraKB, "The KB must be a CameraKB");
        CameraKB cameraKB = (CameraKB) kb;

        log.trace("{}Translating solution [solution={}] >>>", LoggerUtils.tab(), solution);
        List<Constraint> constraints = new LinkedList<>();

        for (Assignment assign: solution.getAssignments()) {
            Constraint constraint = new Constraint(assign.toString());

            translator.translate(assign, cameraKB,
                    constraint.getChocoConstraints(), constraint.getNegChocoConstraints());

            // copy the generated constraints to Solution
            constraint.getChocoConstraints().forEach(solution::addChocoConstraint);
            constraint.getNegChocoConstraints().forEach(solution::addNegChocoConstraint);

            constraints.add(constraint);
        }

        // remove the translated constraints from the Choco model
        // TODO - should move out to the configurator class
        cameraKB.getModelKB().unpost(cameraKB.getModelKB().getCstrs());

        log.debug("{}Translated solution [solution={}] >>>", LoggerUtils.tab(), solution);
        return constraints;
    }
}
