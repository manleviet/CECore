/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fma;

import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.core.FeatureModelException;
import at.tugraz.ist.ase.fm.parser.FMFormat;
import at.tugraz.ist.ase.fm.parser.FeatureModelParser;
import at.tugraz.ist.ase.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.fm.parser.factory.FMParserFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class FMAnalyzerGeneratedModelTest {
    @Disabled("Disabled: Performing only comparison.")
    @Test
    public void test_random_0() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 128
        File fileFM = new File("src/test/resources/random_generated/feature_model_random_0.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Disabled("Disabled: This takes too long.")
    @Test
    public void test_random_1() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 10042
        File fileFM = new File("src/test/resources/random_generated/feature_model_random_1.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Disabled("Disabled: This takes too long.")
    @Test
    public void test_random_2() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 1042
        File fileFM = new File("src/test/resources/random_generated/feature_model_random_2.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Disabled("Disabled: Performing only comparison.")
    @Test
    public void test_random_3() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 169
        File fileFM = new File("src/test/resources/random_generated/feature_model_random_3.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Disabled("Disabled: Performing only comparison.")
    @Test
    public void test_random_4() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 105
        File fileFM = new File("src/test/resources/random_generated/feature_model_random_4_cstr.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    /*
    Performance comparison: Constraints vs. No Constraints
    Number of features: 300 each, in 15 levels
     */

    @Test
    public void test_random_5() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 480
        File fileFM = new File("src/test/resources/random_generated/feature_model_random_5.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Test
    public void test_random_6() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 706
        File fileFM = new File("src/test/resources/random_generated/feature_model_random_6_cstr.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    /*
    End of comparison
    Result: Approx 9.6s / 52.9s
     */
}
