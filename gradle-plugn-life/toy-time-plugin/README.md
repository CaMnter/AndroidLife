# toy-time-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle gradle-plugn-life:toy-time-plugin:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.toytime'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        // local repository
        maven { url uri('../gradle-plugin-life-repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin.toytime:toy-time-plugin:1.0.0'
    }
}
```

<br>


