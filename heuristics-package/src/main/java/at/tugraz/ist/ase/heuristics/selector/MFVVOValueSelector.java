/*
 * MF4ChocoSolver
 *
 * Copyright (c) 2021.
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.heuristics.selector;

import at.tugraz.ist.ase.common.LoggerUtils;
import at.tugraz.ist.ase.heuristics.ValueOrdering;
import lombok.extern.slf4j.Slf4j;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;

@Slf4j
public class MFVVOValueSelector implements IntValueSelector {

    private final List<ValueOrdering> voList;
    int lastIndex = -1;

    public MFVVOValueSelector(List<ValueOrdering> voList){
        this.voList = voList;
    }

    @Override
    public int selectValue(IntVar var) {

        log.trace("{}selectValue: {}", LoggerUtils.tab(), var);
        LoggerUtils.indent();

        ValueOrdering vo = getValueOrdering(var);
        if (vo == null) {
            log.trace("{}NO valueOrdering found - get the lower bound value {}", LoggerUtils.tab(), var.getLB());
            LoggerUtils.outdent();
            return var.getLB();
        }

        if (++lastIndex >= vo.getOrdering().size())
            lastIndex = 0;

//        log.trace("{}valueOrdering found - {}, {}", LoggerUtils.tab(), vo.getVariable().getName(), vo.getOrdering());
//        int lastIndex = vo.getLastSelectedIndex();
//
//        log.trace("{}lastIndex: {}", LoggerUtils.tab(), lastIndex);
//
//        int value = vo.getOrdering().get(lastIndex);
//        log.trace("{}return value {}", LoggerUtils.tab(), vo.getOrdering().get(lastIndex));
//        log.trace("{}lastIndex: {}", LoggerUtils.tab(), vo.getVariable().getDomain().getValue(value));
//        LoggerUtils.outdent();

        return vo.getOrdering().get(lastIndex);
    }

    private ValueOrdering getValueOrdering(IntVar var) {
        return voList.stream().filter(vo -> vo.getIntVar() == var).findFirst().orElse(null);
        /*for (ValueOrdering vo : voList) {
            if (vo.getIntVar() == var) {
                return vo;
            }
        }
        return null;*/
    }
}
