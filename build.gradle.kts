import org.gradle.internal.impldep.org.bouncycastle.cms.RecipientId.password
import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.mybatisflex.plugin"
version = "1.6.1-RELEASE"

repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
//    version.set("2023.2.1")
    version.set("2022.2.5")

//    version.set("2020.3.3")
    type.set("IU") // Target IDE Platform
    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin", "com.intellij.database"))
}

dependencies {
    implementation("com.alibaba.fastjson2:fastjson2:2.0.41")
    implementation("cn.hutool:hutool-core:5.8.22")
    implementation("cn.hutool:hutool-http:5.8.22")
    implementation("com.github.jsqlparser:jsqlparser:4.7")
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    runIde {
        // 启用热重载功能，使用Build菜单编译项目后无需重启调试进程即可完成, 仅支持JBR
        jvmArgs = listOf(
            "-XX:+AllowEnhancedClassRedefinition",
//            "-javaagent:/Users/daijunxiong/Desktop/ja-netfilter-all/ja-netfilter.jar=jetbrains",
//            "--add-opens=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED",
//            "--add-opens=java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED",
        )

    }
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "utf-8"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("202.*")
        untilBuild.set("233.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    publishPlugin {
        token.set(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
    }

}
