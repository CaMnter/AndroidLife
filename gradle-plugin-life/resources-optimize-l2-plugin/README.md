# resources-optimize-l2-plugin

<br>
<br>

## homebrew

[Install Homebrew](https://brew.sh)

<br>

## install tools by homebrew

```shell
brew install cwebp
brew install guetzli
brew install pngquant
``` 
 
<br>

## check tools 

```shell
which cwebp
which guetzli
which pngquant
``` 

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
        classpath 'com.camnter.gradle.plugin:resources-optimize-l2-plugin:1.0.8'
    }
}

// sample
resourcesOptimizeL2Extension {
    webpConvert = false
    debugResourcesSize = true
    debugResourcesOptimize = true
    // 100 kb
    maxSize = 50
    cwebpPath = ''
    guetzliPath = ''
    pngquantPath = ''
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
