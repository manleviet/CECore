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
import at.tugraz.ist.ase.common.ConstraintUtils;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.parser.FMFormat;
import at.tugraz.ist.ase.fm.parser.FeatureModelParser;
import at.tugraz.ist.ase.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.fm.parser.factory.FMParserFactory;
import at.tugraz.ist.ase.fma.analysis.DeadFeatureAnalysis;
import at.tugraz.ist.ase.fma.analysis.VoidFMAnalysis;
import at.tugraz.ist.ase.fma.explanator.DeadFeatureExplanator;
import at.tugraz.ist.ase.fma.explanator.ExplanationColors;
import at.tugraz.ist.ase.fma.explanator.ExplanationUtils;
import at.tugraz.ist.ase.fma.explanator.VoidFMExplanator;
import at.tugraz.ist.ase.kb.core.Constraint;
import at.tugraz.ist.ase.test.ITestCase;
import at.tugraz.ist.ase.test.TestCase;
import at.tugraz.ist.ase.test.TestSuite;
import at.tugraz.ist.ase.test.translator.fm.FMTestCaseTranslator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

class FMAnalyzerTest {
    @Test
    void test() throws FeatureModelParserException, ExecutionException, InterruptedException {
        // load the feature model
        File fileFM = new File("src/test/resources/bamboobike_featureide_void.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        List<ITestCase> testCases = VoidFMAnalysis.createAssumptions(featureModel);
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
    }

    @Test
    void testDeadFeature() throws FeatureModelParserException, ExecutionException, InterruptedException {
        // load the feature model
        File fileFM = new File("src/test/resources/bamboobike_featureide_deadfeature2.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        // create a test case/assumption
        // check void feature model - inconsistent( CF ∪ { c0 })
        List<ITestCase> testCases = VoidFMAnalysis.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(testCases).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        VoidFMAnalysis analysis1 = new VoidFMAnalysis(debuggingModel, testCases.get(0));
        VoidFMExplanator explanator1 = new VoidFMExplanator(debuggingModel, testCases.get(0));

        // create a test case/assumption
        // check dead features - inconsistent( CF ∪ { c0 } U { fi = true })
        testCases = DeadFeatureAnalysis.createAssumptions(featureModel);
        testSuite = TestSuite.builder().testCases(testCases).build();

        debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        DeadFeatureAnalysis analysis2 = new DeadFeatureAnalysis(debuggingModel, testCases.get(6)); // check the feature Female
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
    }
}