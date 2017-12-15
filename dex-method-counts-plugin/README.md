# dex-method-counts-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle :dex-method-counts-plugin:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.dex.method.counts'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        // local repository
        maven { url uri('../gradle-plugin-life/repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin:dex-method-counts-plugin:1.0.4'
    }
}

// sample
dexMethodCountsExtension {
    countFields = false
    includeClasses = false
    printAble = true
    packageFilter = ""
}
```

<br>

## execute

<br>
    
```shell
gradle aD
gradle aR
gradle build
```

<br>
