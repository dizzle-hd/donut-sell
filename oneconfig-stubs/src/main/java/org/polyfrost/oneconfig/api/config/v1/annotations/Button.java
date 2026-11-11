
package org.polyfrost.oneconfig.api.config.v1.annotations;

// Compile-only stub, see oneconfig-stubs/build.gradle.kts.

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Button {
    String title();
    @Deprecated
    boolean titleTranslation() default false;
    @Deprecated
    String titleKey() default "";
    String description() default "";
    @Deprecated
    boolean descriptionTranslation() default false;
    @Deprecated
    String descriptionKey() default "";
    String icon() default "";
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
    String text() default "Click";
    String textKey() default "";
}
