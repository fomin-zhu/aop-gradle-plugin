package com.fomin.aop.demo

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
            implementation 'org.aspectj:aspectjrt:1.9.2'
        }

        variants.all { variant ->
            def fullName = ""
            variant.name.tokenize('-').eachWithIndex { token, index ->
                fullName = fullName + (index == 0 ? token : token.capitalize())
            }
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

                String[] kotlinArgs = ["-showWeaveInfo",
                                       "-1.8",
                                       "-inpath", project.buildDir.path + "/tmp/kotlin-classes/" + fullName,
                                       "-aspectpath", javaCompile.classpath.asPath,
                                       "-d", project.buildDir.path + "/tmp/kotlin-classes/" + fullName,
                                       "-classpath", javaCompile.classpath.asPath,
                                       "-bootclasspath", project.android.bootClasspath.join(
                        File.pathSeparator)]

                MessageHandler handler = new MessageHandler(true)
                new Main().run(args, handler)
                new Main().run(kotlinArgs, handler)
                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break
                        case IMessage.WARNING:
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break
                    }
                }
            }
        }
    }
}