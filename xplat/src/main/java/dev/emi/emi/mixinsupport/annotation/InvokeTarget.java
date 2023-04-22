package dev.emi.emi.mixinsupport.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface InvokeTarget {

	/*
	 * The type to invoke the method on, or either "this" or "super" to assume
	 */
	String owner() default "this";

	/*
	 * The method name if type not NEW
	 */
	String name() default "";

	/*
	 * One of VIRTUAL, SPECIAL, STATIC, INTERFACE, or NEW.
	 * Empty to assume from call site
	 */
	String type() default "";

	/*
	 * Method's signature, or empty to use existing
	 */
	String desc() default "";
}
