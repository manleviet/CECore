package at.tugraz.ist.ase.fma;

import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.core.FeatureModelException;
import at.tugraz.ist.ase.fm.parser.FMFormat;
import at.tugraz.ist.ase.fm.parser.FeatureModelParser;
import at.tugraz.ist.ase.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.fm.parser.factory.FMParserFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class FMAnalyzerPerformanceTest {

    @Test
    public void linuxTest() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 69868
        File fileFM = new File("src/test/resources/linux-2.6.33.3.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Test
    public void mobilemediaTest() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 94
        File fileFM = new File("src/test/resources/mobilemedia2.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Test
    public void weaFQAsTest() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 307
        File fileFM = new File("src/test/resources/WeaFQAs.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Test
    public void busyboxTest() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 1637
        File fileFM = new File("src/test/resources/busybox-1.18.0.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }

    @Test
    public void embtoolkitTest() throws FeatureModelParserException, ExecutionException, FeatureModelException, InterruptedException, CloneNotSupportedException {
        // Lines: 2642
        File fileFM = new File("src/test/resources/embtoolkit.xml");
        FMParserFactory factory = FMParserFactory.getInstance();
        FeatureModelParser parser = factory.getParser(FMFormat.FEATUREIDE);
        FeatureModel featureModel = parser.parse(fileFM);

        FMAnalyzer analyzer = new FMAnalyzer();
        analyzer.performFullAnalysis(featureModel);
    }
}
