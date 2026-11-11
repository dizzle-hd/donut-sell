
package org.polyfrost.oneconfig.api.config.v1.annotations;

// Compile-only stub, see oneconfig-stubs/build.gradle.kts.

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Dropdown {
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
    String[] options() default {};
    @Deprecated
    boolean optionsTranslation() default false;
    @Deprecated
    String[] optionsKey() default {};
}
