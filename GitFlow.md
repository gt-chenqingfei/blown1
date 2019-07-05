
#### Git分支流程

![Alt text](./1557110199655.png)


**1. 基线发布分支(master)**
>基线分支(master)是整体发版的基线分支，用来记录官方的发布轨迹，该分支主要用来做线上打包发布，属于长期分支。该分支禁止直接commit，master分支只存在上车节点，将develop的代码merge过来，打上发布版本tag，用来追溯

**2. 灰度上车分支(develop)**

>develop分支主要用来是做灰度验证后的功能的上车分支，属于长期分支，到达上车时间点，将develop已经经过灰度验证的代码merge到master。该分支禁止用来做feature功能的直接提交，feature功能需要经过灰度，通过cherry-pick合并过来，保证一个feature功能一个提交节点，好做代码回滚

**3. feature分支**

>feature分支负责各个业务模块的开发分支，一般feature分支从最新的master分支拉取，有一定的生命周期，属于临时分支
生命周期：feature分支从master分支拉取，业务功能开发完毕，代码通过git rebase -i (提交sha)做代码合并，cherry-pick到release灰度分支
命名规范：feature/开发者命名缩写-功能-版本；例：feature/cqf/search-2.9.60

**4. 灰度release分支**
>该分支主要用来做灰度版本release，用来做线上灰度，灰度完成后，将数据指标正向的功能cherry-pick到develop
生命周期：release分支从master分支拉取，各个功能feature cherry-pick后，通过打包平台打分支灰度包，收取业务打点数据，将正向功能cherry-pick到develop分支
命名规范：release/版本；例：release/2.9.4

**5. 主线hotfix分支**

>该分支一般会存在很少，master分支的代码都是develop上已经经过分支灰度的功能
生命周期：当主线存在严重crash或者功能feature不可用，需要紧急发版修复，从最新master拉取hotfix分支，bug修复后，经过代码review确保没问题，merge到master和develop分支
命名规范：hotfix/版本；例：hotfix/2.9.4