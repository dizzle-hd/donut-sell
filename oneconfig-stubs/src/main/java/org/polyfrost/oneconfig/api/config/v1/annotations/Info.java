
package org.polyfrost.oneconfig.api.config.v1.annotations;

// Compile-only stub, see oneconfig-stubs/build.gradle.kts.

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Info {
	String title() default "polyui.info";
	@Deprecated
	boolean titleTranslation() default false;
	@Deprecated
	String titleKey() default "";
	String description() default "";
	@Deprecated
	boolean descriptionTranslation() default false;
	@Deprecated
	String descriptionKey() default "";
	String icon() default "polyui/info.svg";
	String category() default "General";
	@Deprecated
	boolean categoryTranslation() default false;
	@Deprecated
	String categoryKey() default "";
	String subcategory() default "General";
	@Deprecated
	boolean subcategoryTranslation() default false;
	@Deprecated
	String subcategoryKey() default "";
}
