##项目开发流程

### 准备阶段
* 进入项目主页http://gitlab.ricebook.net/android/duck，然后fork项目到自己账户下
* 克隆自己的项目http://gitlab.ricebook.net/username/duck到本地, 然后添加远程主账户. 
git remote add gitlab  http://gitlab.ricebook.net/android/duck， 
最后切一个develop 分支做开发
### 更新代码：
git pull --rebase gitlab develop
### 提交代码：
git add .
git commit -m 'comment'
git push origin develop

务必提交代码之前pull 代码
防止代码冲突：尽量提早提代码且避免大面积的格式化代码
### 打包流程
将develop 分支代码合并到 master 分支， 检查是否升级了版本号，最后在主目录下执行 ./gradlew clean build 

## 打渠道包
比如： 
python3 scripts/package.py walle --apk=~/Documents/duck/app/build/outputs/apk/release/duck-RAW-release.apk --channel_file=~/Documents/duck/scripts/channels.txt --output=~/Desktop/android/

##路由使用流程：
注意: 只能跳转到已经声明了的Activity和H5页面，对于想要跳转的对话框和Fragment只能手动跳转

1. 首先在要跳转到的Activity声明url, 用法 @Link(url)， 可以参考`ChannelDetailActivity`的写法 
2. 如果url带有参数，可以声明`@LinkQuery(key_name)`，如果参数可空的话，必须带上@JvmField, 具体可以参考HomeActivity的写法
3. 要让参数生效，必须在onCreate 方法里面执行  bindLinkParams(), 如果该方法报错，先build一次

## 其他
打点神策文档:  https://www.sensorsdata.cn/manual/android_sdk.html
阿里百川文档: http://baichuan.taobao.com/docs/doc.htm?spm=a3c0d.7662649.0.0.b7qDuP&treeId=129&articleId=104528&docType=1

## ToDo
- [✅] 升级targetSdk到28
- [✅] 加入动态权限控制
- [  ] 启用lint 代码检查，删除无用代码，减小包的体积# blown1
