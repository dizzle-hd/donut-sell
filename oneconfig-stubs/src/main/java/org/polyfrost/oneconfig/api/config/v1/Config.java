package org.polyfrost.oneconfig.api.config.v1;

/** Compile-only stub, see oneconfig-stubs/build.gradle.kts. */
public abstract class Config {
    public final String id, title, iconPath;
    public final Category category;

    public Config(String id, String iconPath, String title, Category category) {
        throw new UnsupportedOperationException("stub");
    }

    public Config(String id, String title, Category category) {
        throw new UnsupportedOperationException("stub");
    }

    public void save() {
        throw new UnsupportedOperationException("stub");
    }

    public static final class Category {
        public static final Category COMBAT = null;
        public static final Category QOL = null;
        public static final Category HYPIXEL = null;
        public static final Category OTHER = null;
        public static final Category PERFORMANCE = null;
        public static final Category VISUALS = null;
        public static final Category HUD = null;
        public static final Category UTILITY = null;

        private Category() {
        }
    }
}
