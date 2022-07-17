/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2021-2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce;

import at.tugraz.ist.ase.test.Assignment;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

public class Requirement extends Solution {
    @Builder(builderMethodName = "requirementBuilder")
    public Requirement(@NonNull List<Assignment> assignments) {
        super(assignments);
    }

    @Override
    public String toString() {
        return "Requirement{" +
                "assignments=" + assignments +
                '}';
    }
}
