# FISH-BUILD-TOOL

fish-build-tool 是用 kotlin 编写的一个小工具，主要用来打包 tinker 补丁包，渠道包，及上传 apk 文件到七牛。

工程在源码在 `build-tool` 下面，打包之后的 jar 文件在 `scripts/fish-build-tool-*.jar`

基本使用：

```
java -jar scripts/fish-build-tool-1.0.jar --help
```

### 补丁包

使用

```
java -jar scripts/fish-build-tool-1.0.jar apk-patch --help
```
举例：

```
java -jar scripts/fish-build-tool-1.0.jar apk-patch \
--apk=fish.apk \
--symbol=r.txt
--proguard=proguard_mapping.txt
```

### 渠道包

使用

```
java -jar scripts/fish-build-tool-1.0.jar apk-categoryChannel --help
```
举例：

```
java -jar scripts/fish-build-tool-1.0.jar apk-categoryChannel \
--apk=fish.apk \
--categoryChannel=channels.txt \
--output=~/Downloads/
```

### 上传 APK

使用

```
java -jar scripts/fish-build-tool-1.0.jar apk-upload --help
```

举例：

上传目录

```
java -jar scripts/duck-build-tool-1.1.jar apk-upload \
--folder=~/Desktop/upload/
```

上传文件

``````
java -jar scripts/fish-build-tool-1.0.jar apk-upload a.apk b.apk c.apk
```

### 查看 APK 信息

使用

```
java -jar scripts/fish-build-tool-1.0.jar apk-dump --help
```
举例：

```
java -jar scripts/fish-build-tool-1.0.jar apk-dump a.apk b.apk c.apk
```
