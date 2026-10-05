plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
}
android {
 namespace="com.example.sudokufree"
 compileSdk=35
 defaultConfig {
  applicationId="com.example.sudokufree"
  minSdk=24
  targetSdk=35
  versionCode=3
  versionName="1.0.3"
 }
}
dependencies {
 implementation("androidx.core:core-ktx:1.15.0")
 implementation("androidx.activity:activity-compose:1.10.1")
 implementation("androidx.compose.ui:ui:1.7.8")
 implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
 implementation("androidx.compose.material3:material3:1.3.1")
 implementation("com.google.android.gms:play-services-ads:24.6.0")
}
