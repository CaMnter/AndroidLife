# resources-optimize-l2-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle gradle-plugin-life:resources-optimize-l2-plugin:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.resources.optimize.l2'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        // local repository
        maven { url uri('../gradle-plugin-life/repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin:resources-optimize-l2-plugin:1.0.0'
    }
}

// sample
resourcesOptimizeL2Extension {
    webpConvert = false
    debugResourcesSize = true
    debugResourcesOptimize = false
    // 100 kb
    def maxSize = 100
}
```

<br>

## execute

<br>
    
```shell
gradle clean
gradle resourcesOptimizeL2Debug
gradle resourcesOptimizeL2Release
```

<br>
