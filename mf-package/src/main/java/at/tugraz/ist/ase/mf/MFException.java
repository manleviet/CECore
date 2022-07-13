/*
 * Core components of a configuration environment
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.mf;

public class MFException extends RuntimeException {
    public MFException(String message) {
        super(message);
    }

    public MFException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
