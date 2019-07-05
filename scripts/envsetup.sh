function makeDebug() {
   ./gradlew assembleDebug installDebug --offline -PminSdk=21
   adb shell am start -n 'com.shuashuakan.android/com.shuashuakan.android.SplashActivity'
}

function makeRelease() {
  ./gradlew assembleRelease installRelease --offline -PminSdk=21
  adb shell am start -n 'com.shuashuakan.android/com.shuashuakan.android.SplashActivity'
}

function makeClean() {
  ./gradlew clean
}

function makeUninstall() {
  adb uninstall com.shuashuakan.android
}

