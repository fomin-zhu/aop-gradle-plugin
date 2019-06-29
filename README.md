# aop-gradle-plugin
[上一篇文章](https://www.jianshu.com/p/112cd266fb3f)中讲解了如何在Android使用AOP，会发现在Gradle配置aop会比较麻烦，每个module使用了aop都需要配置。接下来看如何简化配置。
1、创建Module
首先，需要建立一个Android Library，命名为aop-plugin，如图：
![图片](https://images-cdn.shimo.im/QLmJ9zBnuLkisQlE/图片.png!thumbnail)
2、删除文件
由于plugin是由groovy进行创建的，需要删除红色框内的文件
![图片](https://images-cdn.shimo.im/oXyLFq6oAS8UOTuj/图片.png!thumbnail)
3、更改gradle
把module里面的build.gradle内容清空，修改内容：
```
apply plugin: 'groovy'
apply plugin: 'maven'

dependencies {
    compile gradleApi()
    compile localGroovy()
    compile 'com.android.tools.build:gradle:3.1.3'
    compile 'org.aspectj:aspectjtools:1.9.1'
    compile 'org.aspectj:aspectjrt:1.9.1'
}

repositories {
    mavenCentral()
    jcenter()
}


uploadArchives { // 这里只是更新到本地，可以上传到自定义的maven仓库
    repositories {
        mavenDeployer {
            pom.groupId = 'com.fomin.aop.plugin'
            pom.artifactId = 'aop-plugin'
            pom.version = 1.0
            repository(url: uri('../repo'))
        }
    }
}
```
4、创建文件
```
├── build.gradle 
└── src     
    └── main         
        ├── groovy  
            └── com 
                └── fomin      
                    └── aop          
                        └── plugin                     
                            └── AopPlugin.groovy         
        └── resources             
            └── META-INF                 
                └── gradle-plugins                     
                    └── aop-plugin.properties
```
建立/src/main/groovy/com/fomin/aop/plugin/AopPlugin.groovy，其中/src/main/groovy是固定的，/com/fomin/aop/plugin是创建library的包名，AopPlugin.groovy是以groovy的具体类名。
AopPlugin内容：
```
package com.fomin.aop.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class AopPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println("==================")
        println("      Gradle插件         ")
        println("==================")

        if (!project.android) {
            throw new IllegalStateException('Must apply \'com.android.application\' or \'com.android.library\' first!')
        }

        def isApp = project.plugins.withType(AppPlugin)
        def isLib = project.plugins.withType(LibraryPlugin)
        println("isApp : " + isApp)
        println("isLib : " + isLib)
        if (!isApp && !isLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        final def log = project.logger
        final def variants
        if (isApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        project.dependencies {
            implementation 'org.aspectj:aspectjrt:1.9.1'
        }

        variants.all { variant ->
            JavaCompile javaCompile = variant.javaCompile
            javaCompile.doLast {
                String[] args = ["-showWeaveInfo",
                                 "-1.8",
                                 "-inpath", javaCompile.destinationDir.toString(),
                                 "-aspectpath", javaCompile.classpath.asPath,
                                 "-d", javaCompile.destinationDir.toString(),
                                 "-classpath", javaCompile.classpath.asPath,
                                 "-bootclasspath", project.android.bootClasspath.join(
                        File.pathSeparator)]

                MessageHandler handler = new MessageHandler(true);
                new Main().run(args, handler)

                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break;
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break;
                    }
                }
            }
        }
    }
}
```
建立/src/main/resources/META-INF/gradle-plugins/aop-plugin.properties，其中aop-plugin是自定义的插件名称，引用插件的时候用到，其它文件名是固定的。aop-plugin.properties文件内容为：
```
implementation-class=com.fomin.aop.plugin.AopPlugin
```
5、生成plugin
在Gradle栏目中会自动生成一个upload/uploadArchives的脚本，点击这个脚本会在生成 plugin。生成路径在build.gradle已经定义。
![图片](https://images-cdn.shimo.im/kH1iE6fpU3YwQ2OT/图片.png!thumbnail)
![图片](https://images-cdn.shimo.im/xHh0nTkih78MOg2B/图片.png!thumbnail)
6、引入plugin
在相关module或者app的build.gradle引用plugin
```
apply plugin: 'aop-plugin'    ///插件名
```
大功告成，可以直接使用aop plugin，无需再像上一篇文章配置那么繁琐了。
