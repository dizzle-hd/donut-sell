
package org.polyfrost.oneconfig.api.config.v1.annotations;

// Compile-only stub, see oneconfig-stubs/build.gradle.kts.

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ItemList {
    String title();
    @Deprecated
    boolean titleTranslation() default false;
    String description() default "";
    @Deprecated
    boolean descriptionTranslation() default false;
    String icon() default "";
    String category() default "General";
    @Deprecated
    boolean categoryTranslation() default false;
    String subcategory() default "General";
    @Deprecated
    boolean subcategoryTranslation() default false;
    int maxEntries() default 0;
    boolean reorderable() default true;
    String addText() default "oneconfig.itemlist.add";
    @Deprecated
    boolean addTextTranslation() default false;
    boolean fullWidth() default false;
}
