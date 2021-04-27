# android-image-weixin-preview
仿抖音视频预览的封装，上下翻页，简单易用

Imitate Tiktok video preview, turn pages up and down and it is easy to use

### 效果如图：

The example of picture:

![avatar](https://images.ylwx365.com/images/mini/15891619517450576.jpg)
![avatar](https://images.ylwx365.com/images/mini/91851619517450629.jpg)
![avatar](https://images.ylwx365.com/images/mini/86371619517450528.jpg)

### 安装：

Install：

导入除开README.md的上面的文件到项目中即可

Import the above files except README.md into the project

### 使用方法：

How to use：

```java
Intent intent = new Intent(getContext(), CustomVideoPlayerActivity.class);
ArrayList<String> videoUrlList = new ArrayList<>();
videoUrlList.add("your video url");
...

intent.putExtra("currentPosition", 0);// 预览第几个视频
intent.putStringArrayListExtra("videoUrlList", videoUrlList);// 视频链接集合
startActivity(intent);
```

## 欢迎打赏
一分钱也是源源不断技术的推动力

Chinese friends welcome to reward, and foreign friends welcome to give me a star

<img src="https://images.ylwx365.com/images/mini/14911619318881657.jpg" alt="图片加载中.." width="200" />

