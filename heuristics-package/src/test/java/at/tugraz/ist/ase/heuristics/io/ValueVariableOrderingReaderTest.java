/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.heuristics.io;

import at.tugraz.ist.ase.common.IOUtils;
import at.tugraz.ist.ase.heuristics.ValueVariableOrdering;
import at.tugraz.ist.ase.kb.camera.CameraKB;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ValueVariableOrderingReaderTest {

    private final static CameraKB cameraKB = new CameraKB(false);

    @Test
    void test() throws IOException, CsvValidationException {
        InputStream is = IOUtils.getInputStream(this.getClass().getClassLoader(), "ValueVariableOrdering.csv");

        ValueVariableOrderingReader reader = new ValueVariableOrderingReader();
        ValueVariableOrdering vvo = reader.read(is, cameraKB);

        System.out.println(vvo);

        assertNotNull(vvo);
        assertAll(
                () -> assertEquals(2, vvo.getVarOrdering().size()),
                () -> assertEquals(2, vvo.getIntVarOrdering().size()),
                () -> assertEquals(2, vvo.getValueOrdering().size()),
                () -> assertEquals("Display", vvo.getVarOrdering().get(0)),
                () -> assertEquals("Resolution", vvo.getVarOrdering().get(1)),
                () -> assertEquals("Display", vvo.getIntVarOrdering().get(0).getName()),
                () -> assertEquals("Resolution", vvo.getIntVarOrdering().get(1).getName()),
                () -> assertEquals("Display", vvo.getValueOrdering().get(0).getVarName()),
                () -> assertEquals("Resolution", vvo.getValueOrdering().get(1).getVarName()),
                () -> assertEquals(25, vvo.getValueOrdering().get(0).getOrdering().get(0)),
                () -> assertEquals(27, vvo.getValueOrdering().get(0).getOrdering().get(1)),
                () -> assertEquals(30, vvo.getValueOrdering().get(0).getOrdering().get(2)),
                () -> assertEquals(32, vvo.getValueOrdering().get(0).getOrdering().get(3)),
                () -> assertEquals(18, vvo.getValueOrdering().get(0).getOrdering().get(4)),
                () -> assertEquals(102, vvo.getValueOrdering().get(1).getOrdering().get(0)),
                () -> assertEquals(123, vvo.getValueOrdering().get(1).getOrdering().get(1)),
                () -> assertEquals(142, vvo.getValueOrdering().get(1).getOrdering().get(2)),
                () -> assertEquals(162, vvo.getValueOrdering().get(1).getOrdering().get(3)),
                () -> assertEquals(241, vvo.getValueOrdering().get(1).getOrdering().get(4)),
                () -> assertEquals(242, vvo.getValueOrdering().get(1).getOrdering().get(5)),
                () -> assertEquals(208, vvo.getValueOrdering().get(1).getOrdering().get(6)),
                () -> assertEquals(209, vvo.getValueOrdering().get(1).getOrdering().get(7)),
                () -> assertEquals(243, vvo.getValueOrdering().get(1).getOrdering().get(8)),
                () -> assertEquals(363, vvo.getValueOrdering().get(1).getOrdering().get(9)),
                () -> assertEquals(61, vvo.getValueOrdering().get(1).getOrdering().get(10))
        );
    }

}