/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2021-2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce;

import at.tugraz.ist.ase.test.Assignment;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;

@EqualsAndHashCode
@ToString
@Builder
public class Solution implements Cloneable {
    @Getter
    protected List<Assignment> assignments;

    public Solution(@NonNull List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public Assignment getAssignment(int index) {
        checkElementIndex(index, assignments.size(), "Index out of bound!");

        return assignments.get(index);
    }

    public Assignment getAssignment(@NonNull String varName) {
        checkArgument(!varName.isEmpty(), "Variable name cannot be empty!");

        for (Assignment assignment : assignments) {
            if (assignment.getVariable().equals(varName)) {
                return assignment;
            }
        }
        throw new IllegalArgumentException("Variable '" + varName + "' doesn't exist!");
    }

    public int size() {
        return assignments.size();
    }

    public double compare1(Solution solution, int numNULL) {
        int counter = 0;
        for (Assignment assignment1 : assignments) {
            Assignment assignment2 = solution.getAssignment(assignment1.getVariable());

            if (assignment1.equals(assignment2)) {
//                System.out.println( (i+1) + ":\t" + assignment1.getValue() + "-" + assignment2.getValue());
                counter++;
            }
        }

        return (double)(counter - numNULL) / (size() - numNULL);
    }

    public double compare(Solution solution) {
        if (assignments.isEmpty() || this.size() != solution.size()) {
            return 0;
        }

        int counter = 0;
        for (int i = 0; i < assignments.size(); i++) {
            Assignment assignment1 = assignments.get(i);
            Assignment assignment2 = solution.getAssignment(i);

            if (!assignment1.equals(assignment2)) {
                System.out.println( (i+1) + ":\t" + assignment1.getValue() + "-" + assignment2.getValue());
                counter++;
            }
        }

        return (double)counter / size();
    }

    public int getNumNULL() {
        return (int) getAssignments().parallelStream().filter(assignment -> assignment.getValue().equals("NULL")).count();
    }

    public Object clone() throws CloneNotSupportedException {
        Solution clone = (Solution) super.clone();
        // copy assignments
        List<Assignment> assignments = new LinkedList<>();
        for (Assignment assignment : this.assignments) {
            Assignment cloneAssignment = (Assignment) assignment.clone();
            assignments.add(cloneAssignment);
        }
        clone.assignments = assignments;

        return clone;
    }

    public void dispose() {
        assignments = null;
    }
}
