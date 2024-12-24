plugins {
    kotlin("jvm") version "2.1.0"
    java
    application
    id ("org.beryx.jlink") version "3.1.1"
}

group = "com.nero"
version = "0.1"

val jver = 21
val appName = "Draw"
val appVendor = "Nero"

application {
    applicationName = appName
    mainClass = "com.nero.MainKt"
    mainModule = "draw.main"

}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.5.4")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jver))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

jlink {
    options = listOf("--strip-debug",
        "--compress",
        "2",
        "--no-header-files",
        "--no-man-pages"
    )

    jpackage {
        installerOutputDir = project.file("${layout.buildDirectory.get()}/distributions/")
        imageOptions = listOf("--icon", "src/main/resources/icon.ico") //иконка программы
        installerType = "exe" //тип инсталлятора
        installerOutputDir = project.file("${layout.buildDirectory.get()}/distributions/")
        installerOptions = listOf(
            "--win-dir-chooser", //выбор папки установки
//            "--win-per-user-install", //установка только для пользователя
            "--win-menu", // добавить в пуск
            "--win-shortcut", //создание ярлыка и деинсталлятора
            "--icon", //иконка инстялляроа
            "src/main/resources/icon.ico" //путь
        )

        vendor = appVendor
        launcher {
            name = appName
        }
    }
}



//tasks.jar {
//    manifest.attributes["Main-Class"] = "com.nero.MainKt"                                //
//    val dependencies = configurations                                                    //
//        .runtimeClasspath                                                                //
//        .get()                                                                           // Fat Jar
//        .map(::zipTree)                                                                  //
//    from(dependencies)                                                                   //
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE                                      //
//}

sourceSets {
    main {
        kotlin {
            srcDir("src/main/kotlin")
        }
        java {
            srcDir("src/main/kotlin")
        }
    }
}

val createZip = tasks.register<Zip>("createZip") {
    val jpackageDir =  "${layout.buildDirectory.get()}/jpackage/${appName}/"
    from(jpackageDir)
    destinationDirectory.set(project.layout.buildDirectory.dir("distributions"))
    archiveFileName.set("${appName}-${version}.zip")
    dependsOn(tasks.named("jpackageImage"))
    dependsOn(tasks.named("jpackage"))
}