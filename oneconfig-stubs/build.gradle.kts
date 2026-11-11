// Compile-only mirror of the small part of the OneConfig v1 API this mod uses.
// Signatures are copied from https://github.com/Polyfrost/OneConfig (v1.7.0) so the
// compiled mod links against the real classes OneClient ships at runtime.
// Nothing from this project ends up in the mod jar.
plugins { java }

java.toolchain.languageVersion = JavaLanguageVersion.of(25)
tasks.withType<JavaCompile>().configureEach { options.release = 25 }
