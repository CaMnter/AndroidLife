# r2-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle gradle-plugin-life:r2-plugin:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.r2'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        // local repository
        maven { url uri('../gradle-plugin-life/repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin:r2-plugin:1.1.6'
    }
}
```

<br>
