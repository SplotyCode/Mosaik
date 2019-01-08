package de.splotycode.davidlib.console;

import me.david.davidlib.runtime.application.ApplicationType;
import me.david.davidlib.runtime.startup.BootContext;

/**
 * Application type to create Console Applications
 */
public interface ConsoleApplicationType extends ApplicationType {

    default void initType(BootContext context, ConsoleApplicationType dummy) {
    }

    /**
     * Generates a new process bar
     * @param name the name of the process bar
     * @param max the maximum value
     * @param initial the initial value
     * @return the new process bar
     */
    default ProcessBar generateProcessBar(String name, int max, int initial) {
        return new ProcessBar(max, getLogger(), name, initial);
    }

    /**
     * Generates a new process bar
     * @param name the name of the process bar
     * @param max the maximum value
     * @return the new process bar
     */
    default ProcessBar generateProcessBar(String name, int max) {
        return generateProcessBar(name, max, 0);
    }

}
