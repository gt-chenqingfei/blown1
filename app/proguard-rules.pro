# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.MediaPlayer
#-renamesourcefileattribute SourceFile

# http://stackoverflow.com/questions/6974231/proguard-hell-cant-find-referenced-class
-dontwarn javax.xml.stream.events.**

-dontoptimize
-dontwarn org.simalliance.openmobileapi.**
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

# Retrofit, OkHttp, Gson
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
# the okhttp3
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-dontwarn rx.**

#--rxbinding2
-keep class com.jakewharton.rxbinding2.view.ViewScrollChangeEventObservable { *; }
-keep class com.jakewharton.rxbinding2.view.RxViewKt { *; }
-keep class com.jakewharton.rxbinding2.view.ViewScrollChangeEventObservable$* { *; }
-keep class com.jakewharton.rxbinding2.view.ViewScrollChangeEventObservable$Listener { *; }
-keepattributes Exceptions,InnerClasses

# for retrofit2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-dontwarn com.squareup.moshi.**
-keep @com.squareup.moshi.JsonQualifier interface *

-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn com.google.android.gms.ads.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-keep class **$Properties

# Otto
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}
-dontwarn com.android.volley.toolbox.**
-dontwarn com.facebook.infer.**

-dontwarn arrow.GlobalInstances

-keepclassmembers enum * {
 public static **[] values();
 public static ** valueOf(java.lang.String);
 }

-keep class com.ishumei.dfp.SMSDK { *; }

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#umeng share
-dontwarn com.umeng.**
-keep public class com.tencent.** {*;}
-keep public interface com.umeng.socialize.**
-keep public class com.umeng.socialize.* {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.umeng.weixin.handler.**
-keep class com.umeng.weixin.handler.*
-keep class com.umeng.qq.handler.**
-keep class com.umeng.qq.handler.*
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class com.tencent.mm.sdk.** {*;}
-keep class com.tencent.mm.opensdk.** {*;}
-keep class com.tencent.wxop.** {*;}
-keep class com.tencent.mm.sdk.** { *;}
-keep class com.tencent.mm.opensdk.** {*;}
-keep class com.tencent.wxop.** {*;}
-keep class com.tencent.mm.sdk.** {*;}
-keep class com.tencent.tauth.** {*;}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#umeng analytic
-keep class com.umeng.commonsdk.** {*;}

#jpush
-dontoptimize
-dontpreverify
-dontwarn cn.jpush.**
-keep class cn.jpush.** {*;}
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** {*;}
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }
-keep class com.huawei.hms.**{*;}
-dontwarn com.xiaomi.push.**
-keep class com.xiaomi.push.** { *; }
-dontwarn com.coloros.mcsdk.**
-keep class com.coloros.mcsdk.** { *; }

#jMessage
-dontoptimize
-dontpreverify
-keepattributes  EnclosingMethod,Signature
-keepclassmembers class ** {
     public void onEvent*(**);
 }

#========================gson================================
-dontwarn com.google.**
-keep class com.google.gson.** {*;}

#========================protobuf================================
-keep class com.google.protobuf.** {*;}

#========================support=================================
-dontwarn cn.jmessage.support.**
-keep class cn.jmessage.support.**{*;}

#location
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}


-keepattributes *Annotation*
-dontwarn com.tencent.tinker.anno.AnnotationProcessor
-keep @com.tencent.tinker.anno.DefaultLifeCycle public class *
-keep public class * extends android.app.Application {*;}
-keep public class com.tencent.tinker.loader.app.ApplicationLifeCycle {*;}
-keep public class * implements com.tencent.tinker.loader.app.ApplicationLifeCycle {*;}

-keep public class com.tencent.tinker.loader.TinkerLoader {*;}
-keep public class * extends com.tencent.tinker.loader.TinkerLoader {*;}
-keep public class com.tencent.tinker.loader.TinkerTestDexLoad {*;}
-keep public class com.tencent.tinker.loader.TinkerTestAndroidNClassLoader {*;}

#your dex.loader patterns here
-keep class com.tencent.tinker.loader.**


-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
-keep,allowobfuscation @interface com.facebook.soloader.DoNotOptimize

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Do not strip any method/class that is annotated with @DoNotOptimize
-keep @com.facebook.soloader.DoNotOptimize class *
-keepclassmembers class * {
    @com.facebook.soloader.DoNotOptimize *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# sensorsdata
-dontwarn com.sensorsdata.analytics.android.**
-keep class com.sensorsdata.analytics.android.** {
*;
}
-keep class **.R$* {
    <fields>;
}
-keepnames class * implements android.view.View$OnClickListener
-keep public class * extends android.content.ContentProvider
-keepnames class * extends android.view.View

-keep class * extends android.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class * extends android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}

# reflect
-keep public class com.shuashuakan.android.modules.widget.LoopViewPager {*;}
-keep public class com.shuashuakan.android.modules.VideoPlayer {*;}
-keep public class com.shuashuakan.android.utils.DeviceUtils {*;}
-keep public class com.shuashuakan.android.utils.TabLayoutHelper { *; }
-keep public class android.support.design.widget.TabLayout { *; }
-keep class com.shuashuakan.android.spider.** { *; }

-keep public class * extends android.view.View {
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class com.shuashuakan.android.data.api.model.home.LotteryType {
  <init>(...);
  <fields>;
}

#PickerView
-keep class com.bigkoo.pickerview.** { *; }
-keep interface com.bigkoo.pickerview.** { *; }

#PictureSelector 2.0
-keep class com.luck.picture.lib.** { *; }

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

 #rxjava
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#rxandroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#qiniu
-keep class com.qiniu.**{*;}
-keep class com.qiniu.**{public <init>();}
-ignorewarnings
-keep class com.pili.pldroid.player.** { *; }
-keep class com.qiniu.qplayer.mediaEngine.MediaPlayer{*;}

#phototView
-dontwarn com.github.chrisbanes.PhotoView.**
-keep class com.github.chrisbanes.PhotoView.** {*;}

# google的流式布局
-keep class com.google.android.flexbox.** { *; }

#brvah
-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}

# 状态栏
-keep class com.gyf.barlibrary.* {*;}

# 下载文件工具类
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}

# Crashlytics 2.+

-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keepattributes SourceFile, LineNumberTable, *Annotation*

# If you are using custom exceptions, add this line so that custom exception types are skipped during obfuscation:
-keep public class * extends java.lang.Exception

# For Fabric to properly de-

-dontwarn com.tendcloud.tenddata.**
-keep class com.tendcloud.** {*;}
-keep public class com.tendcloud.tenddata.** { public protected *;}
-keepclassmembers class com.tendcloud.tenddata.**{
public void *(***);
}
-keep class com.talkingdata.sdk.TalkingDataSDK {public *;}
-keep class com.apptalkingdata.** {*;}
-keep class dice.** {*; }
-dontwarn dice.**
