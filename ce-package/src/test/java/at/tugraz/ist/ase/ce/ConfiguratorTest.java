/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce;

import at.tugraz.ist.ase.ce.translator.camera.CameraSolutionTranslator;
import at.tugraz.ist.ase.ce.translator.fm.FMSolutionTranslator;
import at.tugraz.ist.ase.ce.writer.TxtSolutionWriter;
import at.tugraz.ist.ase.common.IOUtils;
import at.tugraz.ist.ase.fm.core.AbstractRelationship;
import at.tugraz.ist.ase.fm.core.CTConstraint;
import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.parser.FMParserFactory;
import at.tugraz.ist.ase.fm.parser.FeatureModelParser;
import at.tugraz.ist.ase.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.heuristics.io.ValueVariableOrderingReader;
import at.tugraz.ist.ase.kb.camera.CameraKB;
import at.tugraz.ist.ase.kb.fm.FMKB;
import com.opencsv.exceptions.CsvValidationException;
import lombok.Cleanup;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ConfiguratorTest {

    private final static CameraKB cameraKB = new CameraKB(false);

    private static FMKB<Feature, AbstractRelationship<Feature>, CTConstraint> kb;
    private static FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> featureModel;

    @Test
    void testCameraKB() throws IOException, CsvValidationException {
        InputStream is = IOUtils.getInputStream(this.getClass().getClassLoader(), "vvo_camera.csv");

        ValueVariableOrderingReader reader = new ValueVariableOrderingReader();
        ValueVariableOrdering vvo = reader.read(is, cameraKB);

        System.out.println(vvo);

        Configurator configurator = new Configurator(cameraKB, false, new CameraSolutionTranslator());

        // identify first 5 solutions without the given VVO
        configurator.findSolutions(5, new TxtSolutionWriter("./conf/camera_withoutVVO/"));

        // identify first 5 solutions with the given VVO
        configurator.findSolutions(5, vvo, new TxtSolutionWriter("./conf/camera_withVVO/"));
    }

    @Test
    void testFMKB() throws FeatureModelParserException, IOException, CsvValidationException {
        // read the feature model
        File fileFM = new File("src/test/resources/pizzas.xml");

        @Cleanup("dispose")
        FeatureModelParser<Feature, AbstractRelationship<Feature>, CTConstraint> parser = FMParserFactory.getInstance().getParser(fileFM.getName());
        featureModel = parser.parse(fileFM);

        // convert the feature model into FMKB
        kb = new FMKB<>(featureModel, true);

        // read the value variable ordering
        InputStream is = IOUtils.getInputStream(this.getClass().getClassLoader(), "vvo_pizzas.csv");

        ValueVariableOrderingReader reader = new ValueVariableOrderingReader();
        ValueVariableOrdering vvo = reader.read(is, kb);

        System.out.println(vvo);

        Configurator configurator = new Configurator(kb, true, new FMSolutionTranslator());

        // identify first 5 solutions without the given VVO
        configurator.findSolutions(5, new TxtSolutionWriter("./conf/pizzas_withoutVVO/"));

        // identify first 5 solutions with the given VVO
        configurator.findSolutions(5, vvo, new TxtSolutionWriter("./conf/pizzas_withVVO/"));
    }

    @Test
    void testFindSolutions() throws FeatureModelParserException {
        // read the feature model
        File fileFM = new File("src/test/resources/pizzas.xml");

        @Cleanup("dispose")
        FeatureModelParser<Feature, AbstractRelationship<Feature>, CTConstraint> parser = FMParserFactory.getInstance().getParser(fileFM.getName());
        featureModel = parser.parse(fileFM);

        // convert the feature model into FMKB
        kb = new FMKB<>(featureModel, true);

        Configurator configurator = new Configurator(kb, true, new FMSolutionTranslator());

        int counter = 0;
        for (int i = 0; i < 100; i++) {
            configurator.findSolutions(1);
            System.out.println(++counter + " " + configurator.getLastSolution());
        }

        // check uniqueness of configurations
        List<Solution> solutions = configurator.getSolutions();
        for (int i = 0; i < solutions.size() - 1; i++) {
            for (int j = i + 1; j < solutions.size(); j++) {
                assertNotEquals(solutions.get(i), solutions.get(j));
                if (solutions.get(i).equals(solutions.get(j))) {
                    System.out.println("Solution " + i + " and " + j + " are the same");
                }
            }
        }
    }

    @Test
    void testFind() throws FeatureModelParserException {
        // read the feature model
        File fileFM = new File("src/test/resources/pizzas.xml");

        @Cleanup("dispose")
        FeatureModelParser<Feature, AbstractRelationship<Feature>, CTConstraint> parser = FMParserFactory.getInstance().getParser(fileFM.getName());
        featureModel = parser.parse(fileFM);

        // convert the feature model into FMKB
        kb = new FMKB<>(featureModel, true);

        Configurator configurator = new Configurator(kb, true, new FMSolutionTranslator());
        configurator.prepareSolverWithCurrentConstraints();

        for (int i = 0; i < 100; i++) {
            configurator.find(1, 0, null);
        }

        configurator.resetSolver();
        assert configurator.getNumberSolutions() == 21;

        int counter = 0;
        for (Solution s : configurator.getSolutions()) {
            System.out.println(++counter + " " + s);
        }

        // check uniqueness of configurations
        List<Solution> solutions = configurator.getSolutions();
        for (int i = 0; i < solutions.size() - 1; i++) {
            for (int j = i + 1; j < solutions.size(); j++) {
                assertNotEquals(solutions.get(i), solutions.get(j));
                if (solutions.get(i).equals(solutions.get(j))) {
                    System.out.println("Solution " + i + " and " + j + " are the same");
                }
            }
        }
    }
}