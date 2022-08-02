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
import at.tugraz.ist.ase.fm.parser.FMFormat;
import at.tugraz.ist.ase.fm.parser.FeatureModelParser;
import at.tugraz.ist.ase.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.fm.parser.factory.FMParserFactory;
import at.tugraz.ist.ase.fma.analysis.*;
import at.tugraz.ist.ase.fma.assumption.*;
import at.tugraz.ist.ase.fma.explanator.*;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.test.ITestCase;
import at.tugraz.ist.ase.test.TestCase;
import at.tugraz.ist.ase.test.TestSuite;
import at.tugraz.ist.ase.test.translator.fm.FMTestCaseTranslator;
import com.google.common.collect.Iterators;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class FMAnalyzerTest {
    // TODO: fix all JUnit Assertions.* that have the wrong order! (actual vs. expected)
    @Test
    void testVoidFM() throws FeatureModelParserException, ExecutionException, InterruptedException {
        // load the feature model
        File fileFM = new File("src/test/resources/bamboobike_featureide_void.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        VoidFMAssumption voidFMAssumption = new VoidFMAssumption();
        List<ITestCase> testCases = voidFMAssumption.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        VoidFMAnalysis analysis = new VoidFMAnalysis(debuggingModel, testCases.get(0));
        VoidFMExplanator explanator = new VoidFMExplanator(debuggingModel, testCases.get(0));

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.addAnalysis(analysis, explanator); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X Void feature model");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator.get(), "void feature model"));
        }

        assertFalse(analysis.get());

        List<Set<Constraint>> allDiagnoses = explanator.get();

        Set<Constraint> cs1 = new LinkedHashSet<>();
        cs1.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 8));

        Set<Constraint> cs2 = new LinkedHashSet<>();
        cs2.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 1));

        Set<Constraint> cs3 = new LinkedHashSet<>();
        cs3.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 0));

        assertEquals(3, allDiagnoses.size());
        assertEquals(cs1, allDiagnoses.get(0));
        assertEquals(cs2, allDiagnoses.get(1));
        assertEquals(cs3, allDiagnoses.get(2));
    }

    @Test
    void testDeadFeature_1() throws FeatureModelParserException, ExecutionException, InterruptedException {
        // load the feature model
        File fileFM = new File("src/test/resources/bamboobike_featureide_deadfeature1.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        VoidFMAssumption voidFMAssumption = new VoidFMAssumption();
        List<ITestCase> testCases = voidFMAssumption.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        VoidFMAnalysis analysis1 = new VoidFMAnalysis(debuggingModel, testCases.get(0));
        VoidFMExplanator explanator1 = new VoidFMExplanator(debuggingModel, testCases.get(0));

        // create a test case/assumption
        // check dead features - inconsistent( CF ∪ { c0 } U { fi = true })
        DeadFeatureAssumptions deadFeatureAssumptions = new DeadFeatureAssumptions();
        testCases = deadFeatureAssumptions.createAssumptions(featureModel);
        testSuite = TestSuite.builder().testCases(testCases).build();

        debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        DeadFeatureAnalysis analysis2 = new DeadFeatureAnalysis(debuggingModel, testCases.get(6)); // check the feature Step-through
        DeadFeatureExplanator explanator2 = new DeadFeatureExplanator(debuggingModel, testCases.get(6));

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.addAnalysis(analysis1, explanator1); // add the analysis to the analyzer
        analyzer.addAnalysis(analysis2, explanator2); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis1.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X Void feature model");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator1.get(), "void feature model"));
        }
        if (!analysis2.get()) {
            System.out.println(ExplanationColors.ANOMALY + "X Dead feature");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator2.get(), "dead feature"));
        }

        assertTrue(analysis1.get());
        assertFalse(analysis2.get());

        List<Set<Constraint>> allDiagnoses = explanator2.get();

        Set<Constraint> cs1 = new LinkedHashSet<>();
        cs1.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 8));

        Set<Constraint> cs2 = new LinkedHashSet<>();
        cs2.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 1));

        assertEquals(2, allDiagnoses.size());
        assertEquals(cs1, allDiagnoses.get(0));
        assertEquals(cs2, allDiagnoses.get(1));
    }

    @Test
    void testDeadFeature_2() throws FeatureModelParserException, ExecutionException, InterruptedException {
        // load the feature model
        File fileFM = new File("src/test/resources/bamboobike_featureide_deadfeature2.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        VoidFMAssumption voidFMAssumption = new VoidFMAssumption();
        List<ITestCase> testCases = voidFMAssumption.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        VoidFMAnalysis analysis1 = new VoidFMAnalysis(debuggingModel, testCases.get(0));
        VoidFMExplanator explanator1 = new VoidFMExplanator(debuggingModel, testCases.get(0));

        // create a test case/assumption
        // check dead features - inconsistent( CF ∪ { c0 } U { fi = true })
        DeadFeatureAssumptions deadFeatureAssumptions = new DeadFeatureAssumptions();
        testCases = deadFeatureAssumptions.createAssumptions(featureModel);
        testSuite = TestSuite.builder().testCases(testCases).build();

        debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        DeadFeatureAnalysis analysis2 = new DeadFeatureAnalysis(debuggingModel, testCases.get(6)); // check the feature Step-through
        DeadFeatureExplanator explanator2 = new DeadFeatureExplanator(debuggingModel, testCases.get(6));

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.addAnalysis(analysis1, explanator1); // add the analysis to the analyzer
        analyzer.addAnalysis(analysis2, explanator2); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis1.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X Void feature model");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator1.get(), "void feature model"));
        }
        if (!analysis2.get()) {
            System.out.println(ExplanationColors.ANOMALY + "X Dead feature");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator2.get(), "dead feature"));
        }

        assertTrue(analysis1.get());
        assertFalse(analysis2.get());

        List<Set<Constraint>> allDiagnoses = explanator2.get();

        Set<Constraint> cs1 = new LinkedHashSet<>();
        cs1.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 8));

        Set<Constraint> cs2 = new LinkedHashSet<>();
        cs2.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 4));

        Set<Constraint> cs3 = new LinkedHashSet<>();
        cs3.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 1));

        assertEquals(3, allDiagnoses.size());
        assertEquals(cs1, allDiagnoses.get(0));
        assertEquals(cs2, allDiagnoses.get(1));
        assertEquals(cs3, allDiagnoses.get(2));
    }

    @Test
    void testDeadFeature_3() throws FeatureModelParserException, ExecutionException, InterruptedException {
        // load the feature model
        File fileFM = new File("src/test/resources/bamboobike_featureide_deadfeature3.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        VoidFMAssumption voidFMAssumption = new VoidFMAssumption();
        List<ITestCase> testCases = voidFMAssumption.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        VoidFMAnalysis analysis1 = new VoidFMAnalysis(debuggingModel, testCases.get(0));
        VoidFMExplanator explanator1 = new VoidFMExplanator(debuggingModel, testCases.get(0));

        // create a test case/assumption
        // check dead features - inconsistent( CF ∪ { c0 } U { fi = true })
        DeadFeatureAssumptions deadFeatureAssumptions = new DeadFeatureAssumptions();
        testCases = deadFeatureAssumptions.createAssumptions(featureModel);
        testSuite = TestSuite.builder().testCases(testCases).build();

        debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        DeadFeatureAnalysis analysis2 = new DeadFeatureAnalysis(debuggingModel, testCases.get(3)); // check the feature Drop Handlebar
        DeadFeatureExplanator explanator2 = new DeadFeatureExplanator(debuggingModel, testCases.get(3));

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.addAnalysis(analysis1, explanator1); // add the analysis to the analyzer
        analyzer.addAnalysis(analysis2, explanator2); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis1.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X Void feature model");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator1.get(), "void feature model"));
        }
        if (!analysis2.get()) {
            System.out.println(ExplanationColors.ANOMALY + "X Dead feature");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator2.get(), "dead feature"));
        }

        assertTrue(analysis1.get());
        assertFalse(analysis2.get());

        List<Set<Constraint>> allDiagnoses = explanator2.get();

        Set<Constraint> cs1 = new LinkedHashSet<>();
        cs1.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 8));

        Set<Constraint> cs2 = new LinkedHashSet<>();
        cs2.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 0));
        cs2.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 6));

        Set<Constraint> cs3 = new LinkedHashSet<>();
        cs3.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 0));
        cs3.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 4));

        assertEquals(3, allDiagnoses.size());
        assertEquals(cs1, allDiagnoses.get(0));
        assertEquals(cs2, allDiagnoses.get(1));
        assertEquals(cs3, allDiagnoses.get(2));
    }

    @Test
    void testFullMandatory() throws FeatureModelParserException, ExecutionException, InterruptedException {
        File fileFM = new File("src/test/resources/basic_featureide_fullmandatory1.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check for full mandatory features
        FullMandatoryAssumptions fullMandatoryAssumptions = new FullMandatoryAssumptions();
        List<ITestCase> testCases = fullMandatoryAssumptions.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        FullMandatoryAnalysis analysis = new FullMandatoryAnalysis(debuggingModel, testCases.get(1));
        FullMandatoryExplanator explanator = new FullMandatoryExplanator(debuggingModel, testCases.get(1));

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.addAnalysis(analysis, explanator); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X Full mandatory feature");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator.get(), "full mandatory feature"));
        }

        assertFalse(analysis.get());

        List<Set<Constraint>> allDiagnoses = explanator.get();

        Set<Constraint> cs1 = new LinkedHashSet<>();
        cs1.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 0));

        assertEquals(1, allDiagnoses.size());
        assertEquals(cs1, allDiagnoses.get(0));
    }

    @Test
    void testFalseOptional() throws FeatureModelParserException, ExecutionException, InterruptedException {
        File fileFM = new File("src/test/resources/basic_featureide_falseoptional1.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check for false optional features
        FalseOptionalAssumptions falseOptionalAssumptions = new FalseOptionalAssumptions();
        List<ITestCase> testCases = falseOptionalAssumptions.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        FalseOptionalAnalysis analysis = new FalseOptionalAnalysis(debuggingModel, testCases.get(0));
        FalseOptionalExplanator explanator = new FalseOptionalExplanator(debuggingModel, testCases.get(0));

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.addAnalysis(analysis, explanator); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X False optional feature");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator.get(), "false optional feature"));
        }

        assertFalse(analysis.get());

        List<Set<Constraint>> allDiagnoses = explanator.get();

        Set<Constraint> cs1 = new LinkedHashSet<>();
        cs1.add(Iterators.get(debuggingModel.getPossiblyFaultyConstraints().iterator(), 2));

        assertEquals(1, allDiagnoses.size());
        assertEquals(cs1, allDiagnoses.get(0));
    }

    @Test
    void testConditionallyDead() throws FeatureModelParserException, ExecutionException, InterruptedException {
        File fileFM = new File("src/test/resources/basic_featureide_conditionallydead1.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check for conditionally dead features
        ConditionallyDeadAssumptions conditionallyDeadAssumptions = new ConditionallyDeadAssumptions();
        List<ITestCase> testCases = conditionallyDeadAssumptions.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        ConditionallyDeadAnalysis analysis = new ConditionallyDeadAnalysis(debuggingModel, testCases.get(0));
        ConditionallyDeadExplanator explanator = new ConditionallyDeadExplanator(debuggingModel, testCases.get(0));

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.addAnalysis(analysis, explanator); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X Conditionally dead feature");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator.get(), "conditionally dead feature"));
        }

        assertFalse(analysis.get());

        List<Set<Constraint>> allDiagnoses = explanator.get();

        assertEquals(1, allDiagnoses.size());
    }

    @Test
    public void testMultiple_1() throws FeatureModelParserException, ExecutionException, InterruptedException, FeatureModelException {
        File fileFM = new File("src/test/resources/basic_featureide_multiple1.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        /// VOID FEATURE MODEL
        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        VoidFMAssumption voidFMAssumption = new VoidFMAssumption();
        List<ITestCase> testCases = voidFMAssumption.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        VoidFMAnalysis analysis = new VoidFMAnalysis(debuggingModel, testCases.get(0));
        VoidFMExplanator explanator = new VoidFMExplanator(debuggingModel, testCases.get(0));

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.addAnalysis(analysis, explanator); // add the analysis to the analyzer
        analyzer.run(); // run the analyzer

        // print the result
        ExplanationColors.EXPLANATION = ConsoleColors.WHITE;
        if (analysis.get()) {
            System.out.println(ExplanationColors.OK + "\u2713 Consistency: ok");
        } else {
            System.out.println(ExplanationColors.ANOMALY + "X Void feature model");
            System.out.println(ExplanationUtils.convertToDescriptiveExplanation(explanator.get(), "void feature model"));
        }

        assertTrue(analysis.get());

        /// DEAD FEATURES
        // create a test case/assumption
        // check dead features - inconsistent( CF ∪ { c0 } U { fi = true })
        DeadFeatureAssumptions deadFeatureAssumptions = new DeadFeatureAssumptions();
        List<ITestCase> deadFeatureTestCases = deadFeatureAssumptions.createAssumptions(featureModel);
        TestSuite deadFeatureTestSuite = TestSuite.builder().testCases(deadFeatureTestCases).build();

        FMDebuggingModel deadFeatureDebuggingModel = new FMDebuggingModel(featureModel, deadFeatureTestSuite, new FMTestCaseTranslator(), false, false);
        deadFeatureDebuggingModel.initialize();

        /// FULL MANDATORY
        // create a test case/assumption
        // check full mandatory features
        FullMandatoryAssumptions fullMandatoryAssumptions = new FullMandatoryAssumptions();
        List<ITestCase> fullMandatoryTestCases = fullMandatoryAssumptions.createAssumptions(featureModel);
        TestSuite fullMandatoryTestSuite = TestSuite.builder().testCases(fullMandatoryTestCases).build();

        FMDebuggingModel fullMandatoryDebuggingModel = new FMDebuggingModel(featureModel, fullMandatoryTestSuite, new FMTestCaseTranslator(), false, false);
        fullMandatoryDebuggingModel.initialize();

        /// FALSE OPTIONAL
        // create a test case/assumption
        // check false optional features
        FalseOptionalAssumptions falseOptionalAssumptions = new FalseOptionalAssumptions();
        List<ITestCase> falseOptionalTestCases = falseOptionalAssumptions.createAssumptions(featureModel);
        TestSuite falseOptionalTestSuite = TestSuite.builder().testCases(falseOptionalTestCases).build();

        FMDebuggingModel falseOptionalDebuggingModel = new FMDebuggingModel(featureModel, falseOptionalTestSuite, new FMTestCaseTranslator(), false, false);
        falseOptionalDebuggingModel.initialize();

        /// CONDITIONALLY DEAD
        // create a test case/assumption
        // check conditionally dead features
        ConditionallyDeadAssumptions conditionallyDeadAssumptions = new ConditionallyDeadAssumptions();
        List<ITestCase> conditionallyDeadTestCases = conditionallyDeadAssumptions.createAssumptions(featureModel);
        TestSuite conditionallyDeadTestSuite = TestSuite.builder().testCases(conditionallyDeadTestCases).build();

        FMDebuggingModel conditionallyDeadDebuggingModel = new FMDebuggingModel(featureModel, conditionallyDeadTestSuite, new FMTestCaseTranslator(), false, false);
        conditionallyDeadDebuggingModel.initialize();

        int optWithParent = 0;
        int condDead = 0;
        for (int f = 1; f < featureModel.getNumOfFeatures(); f++) {
            Feature feature = featureModel.getFeature(f);
            boolean anomaly = false;
            // TODO optimise
            boolean isOptionalWithParent = false;
            System.out.println(ConsoleColors.RESET + "[*] Feature: " + feature);

            // create the specified analyses and the corresponding explanators
            DeadFeatureAnalysis deadFeatureAnalysis = new DeadFeatureAnalysis(deadFeatureDebuggingModel, deadFeatureTestCases.get(f - 1));
            DeadFeatureExplanator deadFeatureExplanator = new DeadFeatureExplanator(deadFeatureDebuggingModel, deadFeatureTestCases.get(f - 1));

            // create the specified analyses and the corresponding explanators
            FullMandatoryAnalysis fullMandatoryAnalysis = new FullMandatoryAnalysis(fullMandatoryDebuggingModel, fullMandatoryTestCases.get(f - 1));
            FullMandatoryExplanator fullMandatoryExplanator = new FullMandatoryExplanator(fullMandatoryDebuggingModel, fullMandatoryTestCases.get(f - 1));

            // Check whether feature is optional and has parent
            if (featureModel.isOptionalFeature(feature) && (!featureModel.getMandatoryParents(feature).isEmpty())) {
                isOptionalWithParent = true;
            }

            FalseOptionalAnalysis falseOptionalAnalysis = null;
            FalseOptionalExplanator falseOptionalExplanator = null;
            if (isOptionalWithParent) {
                // create the specified analyses and the corresponding explanators
                falseOptionalAnalysis = new FalseOptionalAnalysis(falseOptionalDebuggingModel, falseOptionalTestCases.get(optWithParent));
                falseOptionalExplanator = new FalseOptionalExplanator(falseOptionalDebuggingModel, falseOptionalTestCases.get(optWithParent));
                optWithParent++;
            }

            ConditionallyDeadAnalysis conditionallyDeadAnalysis = null;
            ConditionallyDeadExplanator conditionallyDeadExplanator = null;
            if (featureModel.isOptionalFeature(feature)) {
                // create the specified analyses and the corresponding explanators
                conditionallyDeadAnalysis = new ConditionallyDeadAnalysis(conditionallyDeadDebuggingModel, conditionallyDeadTestCases.get(condDead));
                conditionallyDeadExplanator = new ConditionallyDeadExplanator(conditionallyDeadDebuggingModel, conditionallyDeadTestCases.get(condDead));
                condDead++;
            }

            analyzer = new FMAnalyzer();
            analyzer.addAnalysis(deadFeatureAnalysis, deadFeatureExplanator); // add the analysis to the analyzer
            analyzer.addAnalysis(fullMandatoryAnalysis, fullMandatoryExplanator); // add the analysis to the analyzer
            if (isOptionalWithParent) {
                analyzer.addAnalysis(falseOptionalAnalysis, falseOptionalExplanator); // add the analysis to the analyzer
            }
            if (featureModel.isOptionalFeature(feature)) {
                analyzer.addAnalysis(conditionallyDeadAnalysis, conditionallyDeadExplanator);
            }
            analyzer.run(); // run the analyzer

            // print the result
            ExplanationColors.EXPLANATION = ConsoleColors.WHITE;

            if (!deadFeatureAnalysis.get()) {
                anomaly = true;
                System.out.println(ExplanationColors.ANOMALY + "X Dead feature");
                System.out.println(ExplanationUtils.convertToDescriptiveExplanation(deadFeatureExplanator.get(), "dead feature"));
            }
            if (!fullMandatoryAnalysis.get()) {
                anomaly = true;
                System.out.println(ExplanationColors.ANOMALY + "X Full mandatory feature");
                System.out.println(ExplanationUtils.convertToDescriptiveExplanation(fullMandatoryExplanator.get(), "full mandatory feature"));
            }
            if (isOptionalWithParent && !falseOptionalAnalysis.get()) {
                anomaly = true;
                System.out.println(ExplanationColors.ANOMALY + "X False optional feature");
                System.out.println(ExplanationUtils.convertToDescriptiveExplanation(falseOptionalExplanator.get(), "false optional feature"));
            }
            if (featureModel.isOptionalFeature(feature) && !conditionallyDeadAnalysis.get()){
                anomaly = true;
                System.out.println(ExplanationColors.ANOMALY + "X Conditionally dead feature");
                System.out.println(ExplanationUtils.convertToDescriptiveExplanation(conditionallyDeadExplanator.get(), "conditionally dead feature"));
            }

            if (!anomaly) {
                System.out.println(ConsoleColors.GREEN + "\u2713 No anomaly found" + ConsoleColors.RESET);
            }
        }


    }
}