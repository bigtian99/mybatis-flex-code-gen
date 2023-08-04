plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "com.mybatisflex.plugin"
version = "1.4.6-RELEASE"

repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
//    version.set("2020.3.3")
    type.set("IU") // Target IDE Platform
    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin", "IntelliLang", "com.intellij.database"))
}

dependencies {
    implementation("com.alibaba.fastjson2:fastjson2:2.0.34")
    implementation("cn.hutool:hutool-core:5.8.21")
    implementation("cn.hutool:hutool-http:5.8.21")
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    runIde {
        // 启用热重载功能，使用Build菜单编译项目后无需重启调试进程即可完成, 仅支持JBR
        jvmArgs = listOf("-XX:+AllowEnhancedClassRedefinition")
    }
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    patchPluginXml {
        sinceBuild.set("202.*")
        untilBuild.set("232.*")
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
