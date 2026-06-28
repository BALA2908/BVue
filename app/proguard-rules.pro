# BVue ProGuard rules (release builds are not minified in v1, so these are minimal).

# NewPipeExtractor uses reflection / Rhino (JS engine) internally — keep its classes.
-keep class org.schabi.newpipe.extractor.** { *; }
-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**
-dontwarn org.schabi.newpipe.extractor.**

# Media3
-dontwarn com.google.common.**
