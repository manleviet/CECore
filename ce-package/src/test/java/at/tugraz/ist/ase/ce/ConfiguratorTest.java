/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce;

import at.tugraz.ist.ase.ce.translator.camera.CameraSolutionTranslator;
import at.tugraz.ist.ase.ce.writer.TxtSolutionWriter;
import at.tugraz.ist.ase.common.IOUtils;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.heuristics.io.ValueVariableOrderingReader;
import at.tugraz.ist.ase.kb.camera.CameraKB;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class ConfiguratorTest {

    private final static CameraKB cameraKB = new CameraKB(false);

    @Test
    void test() throws IOException, CsvValidationException {
        InputStream is = IOUtils.getInputStream(this.getClass().getClassLoader(), "ValueVariableOrdering.csv");

        ValueVariableOrderingReader reader = new ValueVariableOrderingReader();
        ValueVariableOrdering vvo = reader.read(is, cameraKB);

        System.out.println(vvo);

        Configurator configurator = new Configurator(cameraKB, false, new CameraSolutionTranslator());

        // identify 5 first solution without the given VVO
        configurator.findSolutions(5, new TxtSolutionWriter("./conf/withoutVVO/"));

        // identify 5 first solution wit the given VVO
        configurator.findSolutions(5, vvo, new TxtSolutionWriter("./conf/withVVO/"));
    }
}