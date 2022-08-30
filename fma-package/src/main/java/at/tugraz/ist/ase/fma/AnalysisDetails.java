/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma;

import at.tugraz.ist.ase.common.ConsoleColors;
import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fma.analysis.AbstractFMAnalysis;
import at.tugraz.ist.ase.fma.explanator.AbstractAnomalyExplanator;
import at.tugraz.ist.ase.fma.explanator.ExplanationColors;
import at.tugraz.ist.ase.fma.explanator.ExplanationUtils;
import at.tugraz.ist.ase.fma.featuremodel.AnomalyAwareFeature;
import at.tugraz.ist.ase.kb.core.Constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author: Tamim Burgstaller (tamim.burgstaller@student.tugraz.at)
 */
public class AnalysisDetails {
    private List<List<AbstractFMAnalysis<Boolean>>> analyses;
    private List<List<AbstractAnomalyExplanator<List<Set<Constraint>>>>> explanators;
    private int anomaliesFound;
    private Feature feature;
    // TODO explanations!
    // TODO corresponding test cases?
    // TODO sort by feature name?

    public AnalysisDetails(Feature feature) {
        this.analyses = new ArrayList<>(Collections.emptyList());
        this.explanators = new ArrayList<>(Collections.emptyList());
        for (AnomalyType anomalyType : AnomalyType.values()) {
            analyses.add(new ArrayList<>(Collections.emptyList()));
            explanators.add(new ArrayList<>(Collections.emptyList()));
        }
        this.anomaliesFound = 0;
        this.feature = feature;
    }

    public void addAnalysis(AbstractFMAnalysis<Boolean> analysis, AbstractAnomalyExplanator<List<Set<Constraint>>> explanator,
                            AnomalyType checkingAnomaly) {
        analyses.get(checkingAnomaly.ordinal()).add(analysis);
        explanators.get(checkingAnomaly.ordinal()).add(explanator);
    }

    public void printResults() throws ExecutionException, InterruptedException {
        checkResults(true);
    }

    public void checkResults() throws ExecutionException, InterruptedException {
        checkResults(false);
    }

    public void checkResults(boolean print) throws ExecutionException, InterruptedException {
        if (print) {
            System.out.println(ConsoleColors.RESET + "[*] Feature: " + feature);
        }

        for (AnomalyType anomalyType : AnomalyType.values()) {
            for (int a = 0; a < analyses.get(anomalyType.ordinal()).size(); a++) {
                AbstractFMAnalysis<Boolean> analysis = analyses.get(anomalyType.ordinal()).get(a);
                if (!analysis.get()) {
                    switch (anomalyType) {
                        case DEAD -> {
                            anomaliesFound = anomaliesFound | AnomalyType.DEAD.bitValue();
                            ((AnomalyAwareFeature) feature).setAnomalyType(AnomalyType.DEAD); // TODO check whether this works...

                            if (print) {
                                System.out.println(ExplanationColors.ANOMALY + "X Dead feature");
                                System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanators.get(anomalyType.ordinal()).get(a).get(), "dead feature"));
                            }
                        }
                        case FULLMANDATORY -> {
                            anomaliesFound = anomaliesFound | AnomalyType.FULLMANDATORY.bitValue();
                            ((AnomalyAwareFeature) feature).setAnomalyType(AnomalyType.FULLMANDATORY); // TODO check whether this works...

                            if (print) {
                                System.out.println(ExplanationColors.ANOMALY + "X Full mandatory feature");
                                System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanators.get(anomalyType.ordinal()).get(a).get(), "full mandatory feature"));
                            }
                        }
                        case CONDITIONALLYDEAD -> {
                            anomaliesFound = anomaliesFound | AnomalyType.CONDITIONALLYDEAD.bitValue();
                            ((AnomalyAwareFeature) feature).setAnomalyType(AnomalyType.CONDITIONALLYDEAD); // TODO check whether this works...

                            if (print) {
                                System.out.println(ExplanationColors.ANOMALY + "X Conditionally dead feature");
                                System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanators.get(anomalyType.ordinal()).get(a).get(), "conditionally dead feature"));
                            }
                        }
                        case FALSEOPTIONAL -> {
                            anomaliesFound = anomaliesFound | AnomalyType.FALSEOPTIONAL.bitValue();
                            ((AnomalyAwareFeature) feature).setAnomalyType(AnomalyType.FALSEOPTIONAL); // TODO check whether this works...

                            if (print) {
                                System.out.println(ExplanationColors.ANOMALY + "X False optional feature");
                                System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanators.get(anomalyType.ordinal()).get(a).get(), "false optional feature"));
                            }
                        }
                    }
                }
            }
        }

        if (print && anomaliesFound == 0) {
            System.out.println(ConsoleColors.GREEN + "\u2713 No anomaly found" + ConsoleColors.RESET);
        }
    }

    public int getAnomalies() {
        return anomaliesFound;
    }


}
