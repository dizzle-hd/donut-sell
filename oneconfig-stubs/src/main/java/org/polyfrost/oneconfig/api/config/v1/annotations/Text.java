
package org.polyfrost.oneconfig.api.config.v1.annotations;

// Compile-only stub, see oneconfig-stubs/build.gradle.kts.

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Text {
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
    String regex() default "";
    boolean multiline() default false;
    String category() default "General";
    @Deprecated
    String categoryKey() default "";
    @Deprecated
    boolean categoryTranslation() default false;
    String subcategory() default "General";
    @Deprecated
    String subcategoryKey() default "";
    String placeholder() default "oneconfig.textinput.placeholder";
    @Deprecated
    boolean placeholderTranslation() default false;
    @Deprecated
    String placeholderKey() default "";
    boolean fullWidth() default false;
}
