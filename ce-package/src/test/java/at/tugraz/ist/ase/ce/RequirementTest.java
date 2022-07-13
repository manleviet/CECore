/*
 * Core components of a configuration environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce;

import at.tugraz.ist.ase.test.Assignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequirementTest {
    @Test
    void test() {
        Requirement requirement = Requirement.requirementBuilder()
                .assignments(List.of(Assignment.builder().variable("a").value("1").build(),
                        Assignment.builder().variable("b").value("2").build()))
                .build();

        assertEquals(2, requirement.getAssignments().size());
    }
}