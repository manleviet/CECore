/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma;

import at.tugraz.ist.ase.cdrmodel.fm.FMDebuggingModel;
import at.tugraz.ist.ase.cdrmodel.test.ITestCase;
import at.tugraz.ist.ase.cdrmodel.test.TestSuite;
import at.tugraz.ist.ase.cdrmodel.test.translator.fm.FMTestCaseTranslator;
import at.tugraz.ist.ase.common.ConsoleColors;
import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.core.FeatureModelException;
import at.tugraz.ist.ase.fma.analysis.*;
import at.tugraz.ist.ase.fma.assumption.DeadFeatureAssumptions;
import at.tugraz.ist.ase.fma.assumption.FalseOptionalAssumptions;
import at.tugraz.ist.ase.fma.assumption.FullMandatoryAssumptions;
import at.tugraz.ist.ase.fma.assumption.VoidFMAssumption;
import at.tugraz.ist.ase.fma.explanator.*;
import at.tugraz.ist.ase.fma.featuremodel.AnomalyAwareFeatureModel;
import at.tugraz.ist.ase.fma.monitor.IMonitor;
import lombok.NonNull;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 * @author: Tamim Burgstaller (tamim.burgstaller@student.tugraz.at)
 */
public class FMAnalyzer {
    @Setter
    private IMonitor progressMonitor = null;

    private final Map<AbstractFMAnalysis<?>, AbstractAnomalyExplanator<?>> analyses = new LinkedHashMap<>();

    public FMAnalyzer() {
    }

    public void addAnalysis(AbstractFMAnalysis<?> analysis, AbstractAnomalyExplanator<?> explanator) {
        analyses.put(analysis, explanator);
    }

    public void run() throws ExecutionException, InterruptedException {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        for (AbstractFMAnalysis<?> analysis : analyses.keySet()) {
            pool.execute(analysis);
        }

        List<AbstractAnomalyExplanator<?>> runningTasks = new LinkedList<>();
        for (AbstractFMAnalysis<?> analysis : analyses.keySet()) {
            if (!analysis.get()) {
                AbstractAnomalyExplanator<?> explanator = analyses.get(analysis);

                if (explanator != null) {
                    pool.execute(explanator);

                    runningTasks.add(explanator);
                }
            }
        }

        for (AbstractAnomalyExplanator<?> tasks : runningTasks) {
            tasks.join();
        }

        pool.shutdown();
    }

    /**
     * TODO - migrate this function to a new class called ...Builder (e.g. AnalysesBuilder)
     * FMAnalyzer should be simple - focus on the execution of the analyses and explanators
     * The generation of the analyses and explanators should be done in a separate class
     */
    public void performFullAnalysis(@NonNull FeatureModel fm) throws ExecutionException, InterruptedException, CloneNotSupportedException, FeatureModelException {
        FMAnalyzer analyzer = this;
        AnomalyAwareFeatureModel afm = new AnomalyAwareFeatureModel(fm);

        /// VOID FEATURE MODEL
        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        VoidFMAssumption voidFMAssumption = new VoidFMAssumption();
        List<ITestCase> testCases = voidFMAssumption.createAssumptions(afm);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(afm, testSuite, new FMTestCaseTranslator(), false, false, false);
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
        List<AnalysisDetails> details = new ArrayList<>(Collections.emptyList());

        /// DEAD FEATURES
        // create a test case/assumption
        // check dead features - inconsistent( CF ∪ { c0 } U { fi = true })
        DeadFeatureAssumptions deadFeatureAssumptions = new DeadFeatureAssumptions();
        List<ITestCase> deadFeatureTestCases = deadFeatureAssumptions.createAssumptions(afm);
        TestSuite deadFeatureTestSuite = TestSuite.builder().testCases(deadFeatureTestCases).build();

        FMDebuggingModel deadFeatureDebuggingModel = new FMDebuggingModel(afm, deadFeatureTestSuite, new FMTestCaseTranslator(), false, false, false);
        deadFeatureDebuggingModel.initialize();

        for (int f = 1; f < afm.getNumOfFeatures(); f++) {
            details.add(new AnalysisDetails(afm.getFeature(f)));

            // create the specified analyses and the corresponding explanators
            debuggingModelClone = (FMDebuggingModel) deadFeatureDebuggingModel.clone();
            debuggingModelClone.initialize();
            DeadFeatureAnalysis deadFeatureAnalysis = new DeadFeatureAnalysis(debuggingModelClone, deadFeatureTestCases.get(f - 1));
            DeadFeatureExplanator deadFeatureExplanator = new DeadFeatureExplanator(debuggingModelClone, deadFeatureTestCases.get(f - 1));
            analyzer.addAnalysis(deadFeatureAnalysis, deadFeatureExplanator); // add the analysis to the analyzer

            details.get(f - 1).addAnalysis(deadFeatureAnalysis, deadFeatureExplanator, AnomalyType.DEAD);
        }

        analyzer.run(); // run the analyzer

        // Check the results and set dead features - printing will happen later // TODO does this really work?
        for (AnalysisDetails analysisDetails : details) {
            analysisDetails.checkResults();
        }

        /// FULL MANDATORY
        // create a test case/assumption
        // check full mandatory features - inconsistent( CF ∪ { c0 } U { fi = false })
        FullMandatoryAssumptions fullMandatoryAssumptions = new FullMandatoryAssumptions();
        List<ITestCase> fullMandatoryTestCases = fullMandatoryAssumptions.createAssumptions(afm);
        TestSuite fullMandatoryTestSuite = TestSuite.builder().testCases(fullMandatoryTestCases).build();

        FMDebuggingModel fullMandatoryDebuggingModel = new FMDebuggingModel(afm, fullMandatoryTestSuite, new FMTestCaseTranslator(), false, false, false);
        fullMandatoryDebuggingModel.initialize();

        /// FALSE OPTIONAL
        // create a test case/assumption
        // check false optional features  - inconsistent( CF ∪ { c0 } U { fpar = true ^ fopt = false } )
        FalseOptionalAssumptions falseOptionalAssumptions = new FalseOptionalAssumptions();
        List<ITestCase> falseOptionalTestCases = falseOptionalAssumptions.createAssumptions(afm);
        TestSuite falseOptionalTestSuite = TestSuite.builder().testCases(falseOptionalTestCases).build();

        FMDebuggingModel falseOptionalDebuggingModel = new FMDebuggingModel(afm, falseOptionalTestSuite, new FMTestCaseTranslator(), false, false, false);
        falseOptionalDebuggingModel.initialize();

        // CONDITIONALLY DEAD
        // create a test case/assumption
        // check conditionally dead features - inconsistent( CF ∪ { c0 } U { fj = true } U { fi = true } ) for any fj
//        ConditionallyDeadAssumptions conditionallyDeadAssumptions = new ConditionallyDeadAssumptions();
//        List<ITestCase> conditionallyDeadTestCases = conditionallyDeadAssumptions.createAssumptions(afm);
//        TestSuite conditionallyDeadTestSuite = TestSuite.builder().testCases(conditionallyDeadTestCases).build();
//
//        FMDebuggingModel conditionallyDeadDebuggingModel = new FMDebuggingModel(afm, conditionallyDeadTestSuite, new FMTestCaseTranslator(), false, false);
//        conditionallyDeadDebuggingModel.initialize();

        // counting variables for indexes
//        int condDead = 0;
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
            analyzer.addAnalysis(fullMandatoryAnalysis, fullMandatoryExplanator); // add the analysis to the analyzjavaer

            details.get(f - 1).addAnalysis(fullMandatoryAnalysis, fullMandatoryExplanator, AnomalyType.FULLMANDATORY);

            if (afm.isOptionalFeature(feature)) {
                for (int j = 1; j < afm.getNumOfFeatures(); j++) {
                    if (f == j || !afm.isOptionalFeature(afm.getFeature(j)) || afm.getAnomalyAwareFeature(j).isAnomalyType(AnomalyType.DEAD)) {
                        continue;
                    }

                    // create the specified analyses and the corresponding explanators
//                    debuggingModelClone = (FMDebuggingModel) conditionallyDeadDebuggingModel.clone();
//                    debuggingModelClone.initialize();
//                    ConditionallyDeadAnalysis conditionallyDeadAnalysis = new ConditionallyDeadAnalysis(debuggingModelClone, conditionallyDeadTestCases.get(condDead));
//                    ConditionallyDeadExplanator conditionallyDeadExplanator = new ConditionallyDeadExplanator(debuggingModelClone, conditionallyDeadTestCases.get(condDead));
//                    analyzer.addAnalysis(conditionallyDeadAnalysis, conditionallyDeadExplanator); // add the analysis to the analyzer
//
//                    details.get(f - 1).addAnalysis(conditionallyDeadAnalysis, conditionallyDeadExplanator, AnomalyType.CONDITIONALLYDEAD);
//                    condDead++;
                }

                for (Feature parent : afm.getMandatoryParents(feature)) {
                    // create the specified analyses and the corresponding explanators
                    debuggingModelClone = (FMDebuggingModel) falseOptionalDebuggingModel.clone();
                    debuggingModelClone.initialize();
                    FalseOptionalAnalysis falseOptionalAnalysis = new FalseOptionalAnalysis(debuggingModelClone, falseOptionalTestCases.get(optWithParent));
                    FalseOptionalExplanator falseOptionalExplanator = new FalseOptionalExplanator(debuggingModelClone, falseOptionalTestCases.get(optWithParent));
                    analyzer.addAnalysis(falseOptionalAnalysis, falseOptionalExplanator); // add the analysis to the analyzer

                    details.get(f - 1).addAnalysis(falseOptionalAnalysis, falseOptionalExplanator, AnomalyType.FALSEOPTIONAL);
                    optWithParent++;
                }
            }
        }

        analyzer.run(); // run the analyzer

        // Fetch the results
        for (AnalysisDetails analysisDetails : details) {
            analysisDetails.printResults();
        }
    }

    public void performAnalysis(FeatureModel fm, int anomalyTypeBits) {
        // TODO implement single analysis
    }

    // TODO separate test case generation and analysis
    // TODO package application -> lazy checking
    // TODO JSON for test cases
}
