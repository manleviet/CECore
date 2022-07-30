/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma.explanator;

import at.tugraz.ist.ase.common.ConsoleColors;
import lombok.experimental.UtilityClass;

/**
 * Special colors for feature model analysis
 */
@UtilityClass
public class ExplanationColors {
    public String OK = ConsoleColors.BLUE;
    public String ANOMALY = ConsoleColors.RED;
    public String EXPLANATION = ConsoleColors.BLACK;
}
