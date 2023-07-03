import org.gradle.internal.impldep.org.bouncycastle.cms.RecipientId.password
import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.14.2"
}

group = "com.mybatisflex.plugin"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IU") // Target IDE Platform
    plugins.set(listOf("com.intellij.java", "IntelliLang","com.intellij.database"))
}
dependencies {
    implementation("com.alibaba.fastjson2:fastjson2:2.0.34")
    implementation("cn.hutool:hutool-core:5.8.20")
//    implementation("cn.hutool:hutool-crypto:5.8.20")
}

tasks {
    runIde {
    // 启用热重载功能，使用Build菜单编译项目后无需重启调试进程即可完成, 仅支持JBR
        jvmArgs = listOf("-XX:+AllowEnhancedClassRedefinition")
    }
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    publishPlugin {
        token.set( System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
    }

}
