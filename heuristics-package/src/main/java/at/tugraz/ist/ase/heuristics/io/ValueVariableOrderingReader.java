/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.heuristics.io;

import at.tugraz.ist.ase.common.LoggerUtils;
import at.tugraz.ist.ase.heuristics.ValueOrdering;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.kb.core.IIntVarKB;
import at.tugraz.ist.ase.kb.core.IntVariable;
import at.tugraz.ist.ase.kb.core.KB;
import at.tugraz.ist.ase.kb.core.Variable;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.chocosolver.solver.variables.IntVar;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static at.tugraz.ist.ase.common.IOUtils.getCSVReader;

@Slf4j
public class ValueVariableOrderingReader implements IValueVariableOrderingReadable {

    protected ValueOrderingReader valueOrderingReader = new ValueOrderingReader();

    public <K extends KB & IIntVarKB> ValueVariableOrdering read(@NonNull InputStream inputStream, @NonNull K kb) throws CsvValidationException, IOException {
        ValueVariableOrdering vvo = new ValueVariableOrdering();

        CSVReader reader = getCSVReader(inputStream);

        // for each record of ValueVariableOrdering.csv
        String[] items;
        List<String> varOrdering = new LinkedList<>();
        List<IntVar> intVars = new LinkedList<>();
        while ((items = reader.readNext()) != null) {
            // first item is the variable name
            String variableName = items[0];
            // get the variable
            Variable variable = kb.getVariable(variableName);
            IntVar intVar = ((IntVariable) variable).getChocoVar();
            log.trace("{}{}", LoggerUtils.tab(), variable);

            // add the variable to the variable ordering
            varOrdering.add(variable.getName());
            intVars.add(intVar);

            // get the value ordering
            ValueOrdering vo = valueOrderingReader.read(items, variable);
            vvo.setValueOrdering(vo);
        }

        vvo.setVarOrdering(varOrdering);
        vvo.setIntVarOrdering(intVars);

        return vvo;
    }


}
