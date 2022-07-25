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
import at.tugraz.ist.ase.fma.analysis.VoidFMAnalysis;
import at.tugraz.ist.ase.fma.explanator.ExplanationColors;
import at.tugraz.ist.ase.fma.explanator.ExplanationUtils;
import at.tugraz.ist.ase.fma.explanator.VoidFMExplanator;
import at.tugraz.ist.ase.kb.core.Constraint;
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
        TestCase testCase = VoidFMAnalysis.createAssumptions(featureModel);
        TestSuite testSuite = TestSuite.builder().testCases(List.of(testCase)).build();

        FMDebuggingModel debuggingModel = new FMDebuggingModel(featureModel, testSuite, new FMTestCaseTranslator(), false, false);
        debuggingModel.initialize();

        // create the specified analysis and the corresponding explanator
        VoidFMAnalysis analysis = new VoidFMAnalysis(debuggingModel, testCase);
        VoidFMExplanator explanator = new VoidFMExplanator(debuggingModel, testCase);

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
}