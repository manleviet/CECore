/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.heuristics.io;

import at.tugraz.ist.ase.heuristics.ValueOrdering;
import at.tugraz.ist.ase.kb.core.Variable;
import lombok.NonNull;

public interface IValueOrderingReadable {
    ValueOrdering read(@NonNull String[] items, @NonNull Variable variable);
}
