# single-resources-gradle-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle :plugin-life:single-resources:single-resources-gradle-plugin:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.single.resources'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
        maven { url "https://plugins.gradle.org/m2/" }
        google()
        maven {
            url "https://plugins.gradle.org/m2/"
        }
        // local repository
        maven { url uri('../../single-resources-gradle-plugin/repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin:single-resources-gradle-plugin:1.0.2'
    }
}
```

<br>

## execute

<br>
    
```shell
gradle assD
gradle assR
```

<br>
