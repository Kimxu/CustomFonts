
# 概述
> 开发中遇见一个需求，就是需要替换WebView中的字体，因为直接在css样式里面去改字体的话，
>加载页面的时候比较耗费流量，所以就想要在App加载页面的时候去替换字体的样式，把字体样式文件
>放入到app内部的话，app又会增大7、8M，后来想读取系统内部的字体，之后进行加载替换这种方式。

本文主要介绍以下几个内容：

1. 原生控件替换字体
2. WebView替换Assets中的字体
3. WebView替换系统中(system/fonts)的字体

# 讲解

## 原生控件替换字体

原生控件替换字体有很多种方式，这里使用的是Github上的一个开源框架，比较省心好用，具体实现
[Calligraphy](https://github.com/chrisjenx/Calligraphy),字体需要放入到*Assets*
目录中，之后直接使用：`<TextView fontPath="fonts/MyFont.ttf"/>`替换一个样式。

或者替换App中所有的字体样式可以在Application中：

``` Java
@Override
public void onCreate() {
    super.onCreate();
    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
            );
    //....
}
```

之后在使用的Activity中，进行包装：
``` Java
@Override
protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
}
```

*attachBaseContext*方法是在Activity创建之后调用的方法，在这里对Content进行封装，其
Activity中生成Context的代码如下：

``` Java
final void attach(Context context, ActivityThread aThread,  
        Instrumentation instr, IBinder token, int ident,  
        Application application, Intent intent, ActivityInfo info,  
        CharSequence title, Activity parent, String id,  
        NonConfigurationInstances lastNonConfigurationInstances,  
        Configuration config) {  
    attachBaseContext(context);  
```
可见最后会把*Context*传入到*attachBaseContext*中去。

之后启动App，就会发现，字体替换了。


## WebView替换Assets中的字体

替换WebView中的字体有很多种方法，比如在Css样式中加载字体是最简单的方法。不过加载的字体
如果从服务器端加载，那么比较浪费流量，如果从App中加载，因为外网的html不可以加载assets中的
文件，所以使用css样式加载字体有这两种方式：
1. assets中的html页面可以使用Css样式加载assets中字体。
2. w外网的html页面可以使用Css样式加载服务器中的字体。

所以这里使用的方法是在页面加载完成之后在页面中添加段JavaScript,之后拦截Url加载，替换掉其中
的字体。

具体实现在*onPageFinished*方法中：

``` Java
@Override
public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view,url);
    Log.w("loading","onPageFinished "+url);
    view.loadUrl("javascript:!function(){" +
            "s=document.createElement('style');s.innerHTML="
            + "\"@font-face{font-family:kimxuFont;src:url('**injection**/Oswald-Stencbab.ttf');}*{font-family:kimxuFont !important;}\";"
            + "document.getElementsByTagName('head')[0].appendChild(s);" +
            "document.getElementsByTagName('body')[0].style.fontFamily = \"kimxuFont\";}()");
}
```

之后在*shouldInterceptRequest*方法中拦截请求，加载JavaScript中指定的字体：

``` Java
 @Override
public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
    WebResourceResponse response =  super.shouldInterceptRequest(view, url);
    Log.w("loading","shouldInterceptRequest "+url);
    if (url != null && url.contains("**injection**/")) {
        String assertPath = url.substring(url.indexOf("**injection**/") + "**injection**/".length(), url.length());

        try {
            //指定使用assets中的字体
            response = new WebResourceResponse("application/x-font-ttf",
                    "UTF8", getAssets().open("fonts/"+assertPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    return response;
}

```

这样就可以改变WebView中加载的字体了。因为在*onPageFinished*中又重新的*loadUrl*，
所以页面会有一闪的感觉，看具体项目中的要求去衡量是否使用这种方法。
- - -
Ps：

## WebView替换系统中的字体


替换系统中的字体很简单，只需要读取System/fonts里面的字体，之后选取其中的文件转换成
InputStream，在拦截Url的时候进行更改就可以了。

``` Java

private InputStream getSystemFont() {
    String path = "/system/fonts";
    File systemFonts = new File(path);
    File fonts[] = systemFonts.listFiles();
    File useFont =null;
    for (File font : fonts) {
        Log.i("File", font.getName());
        if (TextUtils.equals("DroidSansEthiopic-Regular.ttf",font.getName())){
            useFont=font;
            break;
        }
    }
    InputStream fontInput;
    try {
        fontInput = new FileInputStream(useFont);
    } catch (FileNotFoundException e) {
        Log.i("Error",e.getLocalizedMessage());
        return null;
    }
    return fontInput;
}

```

在*shouldInterceptRequest*中：
``` Java
// 使用系统字体
response = new WebResourceResponse("application/x-font-ttf",
                           "UTF8", systemFont);

```

到这里本片文章就结束了，谢谢大家。[更多点我](https://kimxu.herokuapp.com/posts/change_webview_font/)

