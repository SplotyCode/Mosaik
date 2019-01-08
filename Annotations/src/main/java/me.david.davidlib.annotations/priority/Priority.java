package me.david.davidlib.annotations.priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Symbolises a specific priority for a class
 * For example it is used to device what StartUp Tasks should run first
 * @see Last
 * @see First
 * @see me.david.davidlib.annotations.AnnotationHelper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Priority {

    /**
     * Returns the Priority
     * The higher the important's of the class
     * @return the priority of the class
     */
    int priority() default 0;

}
