<h1 align="center">fofa_viewer </h1>
<p align="center"> 
<img src="https://img.shields.io/badge/version-1.0.0-brightgreen">
<img src="https://img.shields.io/badge/author-f1ashine-orange">
<img src="https://img.shields.io/badge/WgpSec-%E7%8B%BC%E7%BB%84%E5%AE%89%E5%85%A8%E5%9B%A2%E9%98%9F-blue">
</p>



![](https://f1ashine.gitee.io/research_pic/fofa_viewer/ui.jpg)
##  简介
一个简单易用的fofa客户端  
使用javafx编写，便于跨平台使用
## :sparkles: 功能
1. 多标签式查询结果展示
2. 丰富的右键菜单
3. 支持查询结果导出
4. 支持手动修改查询最大条数，方便非高级会员使用(修改`config.properties`中的`maxSize`即可)

<img src="https://f1ashine.gitee.io/research_pic/fofa_viewer/contextMenu.png">

## :rocket: 手动编译
idea打开项目，等待依赖包下载完毕后直接双击Plugins-assembly-assembly:assembly，然后运行target文件夹中带有"with-dependencies"的jar包即可。

## ⚠️ 说明
使用前需要在`config.properties`中配置`email`和`key`才能正常使用  
项目中配置了error.log，如果有需要提bug，希望能带上这个截图，另外也欢迎提issue帮助改进。

[![Stargazers over time](https://starchart.cc/wgpsec/fofa_viewer.svg)](https://starchart.cc/wgpsec/fofa_viewer)


