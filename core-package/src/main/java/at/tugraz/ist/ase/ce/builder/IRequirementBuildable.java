/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce.builder;

import at.tugraz.ist.ase.ce.Requirement;
import lombok.NonNull;

public interface IRequirementBuildable {
    Requirement build(@NonNull String stringUR);
}
