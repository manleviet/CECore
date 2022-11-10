/*
 * CECore - Core components of a Configuration Environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.ce.writer;

import at.tugraz.ist.ase.ce.Solution;
import lombok.NonNull;

import java.io.FileWriter;
import java.io.IOException;

public abstract class SolutionWriter {

    protected final String folder;
    protected static int counter = 0;

    protected FileWriter fileWriter;

    public SolutionWriter(String folder) {
        this.folder = folder;
    }

    public abstract void write(@NonNull Solution solution) throws IOException;

    protected void createFileWriter() throws IOException {
        counter++;
        fileWriter = new FileWriter(String.format(folder + "conf_%s.xml", counter));
    }
}
