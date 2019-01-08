package me.david.davidlib.annotations.priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Symbolises that the class has the highest priority
 * Equals to <code>@Priority(Integer.MAX_VALUE)</code>
 * @see Priority
 * @see Last
 * @see me.david.davidlib.annotations.AnnotationHelper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface First {

}
