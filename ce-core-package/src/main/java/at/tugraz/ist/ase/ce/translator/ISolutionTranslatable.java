/*
 * MF4ChocoSolver
 *
 * Copyright (c) 2022.
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce.translator;

import at.tugraz.ist.ase.ce.Solution;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.kb.core.KB;
import lombok.NonNull;

import java.util.List;

public interface ISolutionTranslatable {
    Constraint translate(@NonNull Solution solution, @NonNull KB kb);

    List<Constraint> translateToList(@NonNull Solution solution, @NonNull KB kb);
}
