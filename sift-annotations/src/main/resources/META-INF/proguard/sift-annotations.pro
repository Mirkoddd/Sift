# Sift Annotations Proguard / R8 Rules
# These rules are automatically consumed by the Android Gradle Plugin

# Protect all custom annotations from being obfuscated or removed
-keep @interface com.mirkoddd.sift.annotations.** { *; }

# Protect the validator so Jakarta/Hibernate/Spring can instantiate it via reflection
-keep class com.mirkoddd.sift.annotations.SiftMatchValidator { *; }

# Protect the Regex provider interface
-keep interface com.mirkoddd.sift.annotations.SiftRegexProvider { *; }