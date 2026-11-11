
package org.polyfrost.oneconfig.api.config.v1.annotations;

// Compile-only stub, see oneconfig-stubs/build.gradle.kts.

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Number {
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
    String unit() default "";
    @Deprecated
    boolean unitTranslation() default false;
    @Deprecated
    String unitKey() default "";
    float min() default -10f;
    float max() default 100f;
    String placeholder() default "oneconfig.numberinput.placeholder";
    @Deprecated
    boolean placeholderTranslation() default false;
    @Deprecated
    String placeholderKey() default "";
}
