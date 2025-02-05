plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.mybatisflex.plugin"
version = "1.6.6-RELEASE"

repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
//    version.set("2023.2.6")
    version.set("2024.1")

//    version.set("2022.2.5")
    type.set("IU") // Target IDE Platform
    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin", "com.intellij.database","com.intellij.spring.boot"))
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")
    implementation("cn.hutool:hutool-core:5.8.29")
    implementation("cn.hutool:hutool-http:5.8.29")
    implementation("com.github.jsqlparser:jsqlparser:4.9")
    implementation(fileTree(mapOf("dir" to "libs", "includes" to listOf("*.jar"))))
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")  // 注解处理器依赖
    // 测试相关的Lombok支持
    testCompileOnly("org.projectlombok:lombok:1.18.34")

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
//            "-javaagent:/Users/daijunxiong/app/jetbra/ja-netfilter.jar=jetbrains",
//            "--add-opens=java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED",
//            "--add-opens=java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED"

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
        sinceBuild.set("232.*")
        untilBuild.set("242.*")
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
