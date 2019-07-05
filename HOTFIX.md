# 热修复打包流程

### 安装

`brew install apktool`
`brew install python3`
`pip3 install click`

多快好省使用 tinker 作为热修复方案，补丁包发布管理则使用 [bugly](https://bugly.qq.com) 提供的服务。

下面介绍的是热修复的流程，我们假设线上已经发布 1.0.0 版本，代码在 master 分支。

1. checkout
从 master 分支切出一个名为 `v1.0.0_hotfix` 的分支

2. bug fix
在新切出的分支修复 bug

3. 测试并提交代码
4. 打 patch 包
   在工程根目录下执行

   ```
   python3 scripts/package.py patch --apk=your.apk \
       --symbol_file=app-release-R.txt \
       --proguard_file=app-release-mapping.txt
   ```
   补丁包在 `${projectDir}/build/outputs/patch` 目录下
5. 测试打包的 patch 补丁
6. 提交 bugly 后台，发布补丁
7. `v1.0.0_hotfix` 分支的代码改动同时提交到 `master` `develop` 分支

更多请参考 bugly [文档](https://bugly.qq.com/docs/user-guide/instruction-manual-android-hotfix-demo/?v=20170912151050)

### tinker id 生成规则

#### 基准包

```groovy
fish_${versionName}_${gitSha()}
```
