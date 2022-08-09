/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma;

import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.common.ConsoleColors;
import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.core.FeatureModelException;
import at.tugraz.ist.ase.fma.analysis.*;
import at.tugraz.ist.ase.fma.assumption.*;
import at.tugraz.ist.ase.fma.explanator.*;
import at.tugraz.ist.ase.fma.featuremodel.AnomalyAwareFeature;
import at.tugraz.ist.ase.fma.featuremodel.AnomalyAwareFeatureModel;
import at.tugraz.ist.ase.fma.monitor.IMonitor;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.test.ITestCase;
import at.tugraz.ist.ase.test.TestSuite;
import at.tugraz.ist.ase.test.translator.fm.FMTestCaseTranslator;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FMAnalyzer {
    @Setter
    private IMonitor progressMonitor = null;

    private final Map<AbstractFMAnalysis<Boolean>, AbstractAnomalyExplanator> analyses = new LinkedHashMap<>();

    public FMAnalyzer() {
    }

    public void addAnalysis(AbstractFMAnalysis<Boolean> analysis, AbstractAnomalyExplanator explanator) {
        analyses.put(analysis, explanator);
    }

    public void run() throws ExecutionException, InterruptedException {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        for (AbstractFMAnalysis<Boolean> analysis : analyses.keySet()) {
            pool.execute(analysis);
        }

        List<AbstractAnomalyExplanator> runningTasks = new LinkedList<>();
        for (AbstractFMAnalysis<Boolean> analysis : analyses.keySet()) {
            if (!analysis.get()) {
                AbstractAnomalyExplanator explanator = analyses.get(analysis);
                pool.execute(explanator);

                runningTasks.add(explanator);
            }
        }

        for (AbstractAnomalyExplanator tasks : runningTasks) {
            tasks.join();
        }

        pool.shutdown();
    }

    public void performFullAnalysis(@NonNull FeatureModel fm) throws ExecutionException, InterruptedException, CloneNotSupportedException, FeatureModelException {
        FMAnalyzer analyzer = this;
        AnomalyAwareFeatureModel afm = new AnomalyAwareFeatureModel(fm);

        /// VOID FEATURE MODELfeatureModel
        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        VoidFMAssumption voidFMAssumption = new VoidFMAssumption();
        List<ITestCase> testCases = voidFMAssumption.createAssumptions(afm);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(afm, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        VoidFMAnalysis analysis = new VoidFMAnalysis(debuggingModel, testCases.get(0));
        VoidFMExplanator explanator = new VoidFMExplanator(debuggingModel, testCases.get(0));

        analyzer.addAnalysis(analysis, explanator); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X Void feature model");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator.get(), "void feature model"));
            return;
        }

        FMDebuggingModel debuggingModelClone = null;

        // Store all analyses in here to access them later
        List<List<AbstractFMAnalysis<Boolean>>> allAnalyses = new ArrayList<>(Collections.emptyList());
        List<List<AbstractAnomalyExplanator<List<Set<Constraint>>>>> allExplanators = new ArrayList<>(Collections.emptyList());
        List<List<AnomalyType>> anomalyTypes = new ArrayList<>(Collections.emptyList());

        /// DEAD FEATURES
        // create a test case/assumption
        // check dead features - inconsistent( CF ∪ { c0 } U { fi = true })
        DeadFeatureAssumptions deadFeatureAssumptions = new DeadFeatureAssumptions();
        List<ITestCase> deadFeatureTestCases = deadFeatureAssumptions.createAssumptions(afm);
        TestSuite deadFeatureTestSuite = TestSuite.builder().testCases(deadFeatureTestCases).build();

        FMDebuggingModel deadFeatureDebuggingModel = new FMDebuggingModel(afm, deadFeatureTestSuite, new FMTestCaseTranslator(), false, false);
        deadFeatureDebuggingModel.initialize();

        for (int f = 1; f < afm.getNumOfFeatures(); f++) {
            allAnalyses.add(new ArrayList<>(Collections.emptyList()));
            allExplanators.add(new ArrayList<>(Collections.emptyList()));
            anomalyTypes.add(new ArrayList<>(Collections.emptyList()));

            // create the specified analyses and the corresponding explanators
            debuggingModelClone = (FMDebuggingModel) deadFeatureDebuggingModel.clone();
            debuggingModelClone.initialize();
            DeadFeatureAnalysis deadFeatureAnalysis = new DeadFeatureAnalysis(debuggingModelClone, deadFeatureTestCases.get(f - 1));
            DeadFeatureExplanator deadFeatureExplanator = new DeadFeatureExplanator(debuggingModelClone, deadFeatureTestCases.get(f - 1));
            analyzer.addAnalysis(deadFeatureAnalysis, deadFeatureExplanator); // add the analysis to the analyzer

            allAnalyses.get(f - 1).add(deadFeatureAnalysis);
            allExplanators.get(f - 1).add(deadFeatureExplanator);
            anomalyTypes.get(f - 1).add(AnomalyType.DEAD);
        }

        analyzer.run(); // run the analyzer

        // Check the results and set dead features - printing will happen later
        for (int f = 1; f < afm.getNumOfFeatures(); f++) {
            for (int runningAnalysis = 0; runningAnalysis < allAnalyses.get(f - 1).size(); runningAnalysis++) {
                if (!allAnalyses.get(f - 1).get(runningAnalysis).get()) {
                    afm.getAnomalyAwareFeature(f).setAnomalyType(AnomalyType.DEAD);
                }
            }
        }

        /// FULL MANDATORY
        // create a test case/assumption
        // check full mandatory features - inconsistent( CF ∪ { c0 } U { fi = false })
        FullMandatoryAssumptions fullMandatoryAssumptions = new FullMandatoryAssumptions();
        List<ITestCase> fullMandatoryTestCases = fullMandatoryAssumptions.createAssumptions(afm);
        TestSuite fullMandatoryTestSuite = TestSuite.builder().testCases(fullMandatoryTestCases).build();

        FMDebuggingModel fullMandatoryDebuggingModel = new FMDebuggingModel(afm, fullMandatoryTestSuite, new FMTestCaseTranslator(), false, false);
        fullMandatoryDebuggingModel.initialize();

        /// FALSE OPTIONAL
        // create a test case/assumption
        // check false optional features  - inconsistent( CF ∪ { c0 } U { fpar = true ^ fopt = false } )
        FalseOptionalAssumptions falseOptionalAssumptions = new FalseOptionalAssumptions();
        List<ITestCase> falseOptionalTestCases = falseOptionalAssumptions.createAssumptions(afm);
        TestSuite falseOptionalTestSuite = TestSuite.builder().testCases(falseOptionalTestCases).build();

        FMDebuggingModel falseOptionalDebuggingModel = new FMDebuggingModel(afm, falseOptionalTestSuite, new FMTestCaseTranslator(), false, false);
        falseOptionalDebuggingModel.initialize();

        // CONDITIONALLY DEAD
        // create a test case/assumption
        // check conditionally dead features - inconsistent( CF ∪ { c0 } U { fj = true } U { fi = true } ) for any fj
        ConditionallyDeadAssumptions conditionallyDeadAssumptions = new ConditionallyDeadAssumptions();
        List<ITestCase> conditionallyDeadTestCases = conditionallyDeadAssumptions.createAssumptions(afm);
        TestSuite conditionallyDeadTestSuite = TestSuite.builder().testCases(conditionallyDeadTestCases).build();

        FMDebuggingModel conditionallyDeadDebuggingModel = new FMDebuggingModel(afm, conditionallyDeadTestSuite, new FMTestCaseTranslator(), false, false);
        conditionallyDeadDebuggingModel.initialize();

        // counting variables for indexes
        int condDead = 0;
        int optWithParent = 0;
        for (int f = 1; f < afm.getNumOfFeatures(); f++) {
            if (afm.getAnomalyAwareFeature(f).isAnomalyType(AnomalyType.DEAD)) {
                continue;
            }

            Feature feature = afm.getFeature(f);

            // create the specified analyses and the corresponding explanators
            debuggingModelClone = (FMDebuggingModel) fullMandatoryDebuggingModel.clone();
            debuggingModelClone.initialize();
            FullMandatoryAnalysis fullMandatoryAnalysis = new FullMandatoryAnalysis(debuggingModelClone, fullMandatoryTestCases.get(f - 1));
            FullMandatoryExplanator fullMandatoryExplanator = new FullMandatoryExplanator(debuggingModelClone, fullMandatoryTestCases.get(f - 1));
            analyzer.addAnalysis(fullMandatoryAnalysis, fullMandatoryExplanator); // add the analysis to the analyzer

            allAnalyses.get(f - 1).add(fullMandatoryAnalysis);
            allExplanators.get(f - 1).add(fullMandatoryExplanator);
            anomalyTypes.get(f - 1).add(AnomalyType.FULLMANDATORY);

            if (afm.isOptionalFeature(feature)) {
                for (int j = 1; j < afm.getNumOfFeatures(); j++) {
                    if (f == j || !afm.isOptionalFeature(afm.getFeature(j)) || afm.getAnomalyAwareFeature(j).isAnomalyType(AnomalyType.DEAD)) {
                        continue;
                    }

                    // create the specified analyses and the corresponding explanators
                    debuggingModelClone = (FMDebuggingModel) conditionallyDeadDebuggingModel.clone();
                    debuggingModelClone.initialize();
                    ConditionallyDeadAnalysis conditionallyDeadAnalysis = new ConditionallyDeadAnalysis(debuggingModelClone, conditionallyDeadTestCases.get(condDead));
                    ConditionallyDeadExplanator conditionallyDeadExplanator = new ConditionallyDeadExplanator(debuggingModelClone, conditionallyDeadTestCases.get(condDead));
                    analyzer.addAnalysis(conditionallyDeadAnalysis, conditionallyDeadExplanator); // add the analysis to the analyzer

                    allAnalyses.get(f - 1).add(conditionallyDeadAnalysis);
                    allExplanators.get(f - 1).add(conditionallyDeadExplanator);
                    anomalyTypes.get(f - 1).add(AnomalyType.CONDITIONALLYDEAD);
                    condDead++;
                }

                for (Feature parent : afm.getMandatoryParents(feature)) {
                    // create the specified analyses and the corresponding explanators
                    debuggingModelClone = (FMDebuggingModel) falseOptionalDebuggingModel.clone();
                    debuggingModelClone.initialize();
                    FalseOptionalAnalysis falseOptionalAnalysis = new FalseOptionalAnalysis(debuggingModelClone, falseOptionalTestCases.get(optWithParent));
                    FalseOptionalExplanator falseOptionalExplanator = new FalseOptionalExplanator(debuggingModelClone, falseOptionalTestCases.get(optWithParent));
                    analyzer.addAnalysis(falseOptionalAnalysis, falseOptionalExplanator); // add the analysis to the analyzer

                    allAnalyses.get(f - 1).add(falseOptionalAnalysis);
                    allExplanators.get(f - 1).add(falseOptionalExplanator);
                    anomalyTypes.get(f - 1).add(AnomalyType.FALSEOPTIONAL);
                    optWithParent++;
                }
            }
        }

        analyzer.run(); // run the analyzer

        // Fetch the results
        for (int f = 1; f < afm.getNumOfFeatures(); f++) {
            System.out.println(ConsoleColors.RESET + "[*] Feature: " + afm.getFeature(f));

            // print the result
            ExplanationColors.EXPLANATION = ConsoleColors.WHITE;

            boolean anomaly = false;
            for (int runningAnalysis = 0; runningAnalysis < allAnalyses.get(f - 1).size(); runningAnalysis++) {
                if (!allAnalyses.get(f - 1).get(runningAnalysis).get()) {
                    anomaly = true;
                    switch (anomalyTypes.get(f - 1).get(runningAnalysis)) {
                        case DEAD -> {
                            System.out.println(ExplanationColors.ANOMALY + "X Dead feature");
                            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(allExplanators.get(f - 1).get(runningAnalysis).get(), "dead feature"));
                        }
                        case FULLMANDATORY -> {
                            System.out.println(ExplanationColors.ANOMALY + "X Full mandatory feature");
                            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(allExplanators.get(f - 1).get(runningAnalysis).get(), "full mandatory feature"));
                        }
                        case CONDITIONALLYDEAD -> {
                            System.out.println(ExplanationColors.ANOMALY + "X Conditionally dead feature");
                            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(allExplanators.get(f - 1).get(runningAnalysis).get(), "conditionally dead feature"));
                        }
                        case FALSEOPTIONAL -> {
                            System.out.println(ExplanationColors.ANOMALY + "X False optional feature");
                            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(allExplanators.get(f - 1).get(runningAnalysis).get(), "false optional feature"));
                        }
                    }
                }
            }
            if (!anomaly) {
                System.out.println(ConsoleColors.GREEN + "\u2713 No anomaly found" + ConsoleColors.RESET);
            }
        }
    }

    public void performAnalysis(FeatureModel fm, int anomalyTypeBits) {
        // TODO implement single analysis
    }
}
