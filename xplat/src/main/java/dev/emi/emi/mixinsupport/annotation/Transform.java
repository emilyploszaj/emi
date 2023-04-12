package dev.emi.emi.mixinsupport.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Transform {
	
	/*
	 * The method/field name
	 */
	String name() default "";

	/*
	 * The method/field signature
	 */
	String desc() default "";

	/*
	 * One of PUBLIC, PRIVATE, PROTECTED, or PACKAGE, or empty for no change
	 */
	String visibility() default "";
}
