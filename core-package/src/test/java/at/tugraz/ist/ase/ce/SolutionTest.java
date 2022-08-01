/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce;

import at.tugraz.ist.ase.kb.core.Assignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolutionTest {
    @Test
    void testToString() {
        Assignment a1 = Assignment.builder().variable("a1").value("a1").build();
        Assignment a2 = Assignment.builder().variable("a2").value("a2").build();

        Solution s = Solution.builder().assignments(List.of(a1, a2)).build();

        assertEquals("a1=a1, a2=a2", s.toString());
    }
}