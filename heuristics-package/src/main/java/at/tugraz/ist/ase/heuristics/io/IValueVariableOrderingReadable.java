/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.heuristics.io;

import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.kb.core.KB;
import com.opencsv.exceptions.CsvValidationException;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;

public interface IValueVariableOrderingReadable {
    <K extends KB> ValueVariableOrdering read(@NonNull InputStream inputStream, @NonNull K kb) throws CsvValidationException, IOException;
}
