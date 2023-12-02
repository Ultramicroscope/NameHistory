plugins {
    java
    id("com.github.weave-mc.weave-gradle") version "649dba7468"
}

group = "wtf.ultra"
version = "1.0.1"

minecraft.version("1.8.9")

repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.weave-mc:weave-loader:v0.2.4")
}

tasks.compileJava {
    options.release.set(11)
}
