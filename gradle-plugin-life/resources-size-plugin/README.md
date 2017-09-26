# resources-size-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle gradle-plugin-life:resources-size-plugin:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.resources.size'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        // local repository
        maven { url uri('../gradle-plugin-life/repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin.life:resources-size-plugin:1.1.0'
    }
}

// sample
resourcesSizeExtension {
    debugAble = true
    // default 100 kb
    maxSize = 50
}
```

<br>

## execute

<br>
    
```shell
gradle clean
gradle resourcesSizeDebug
gradle resourcesSizeRelease
```

<br>
