# gradle-plugin-life

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle :gradle-plugin-life:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.life'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        // local repository
        maven { url uri('../gradle-plugin-life-repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin.life:gradle-plugin-life:1.0.2'
    }
}

// sample
lifeExtension {
    id = "[App Module]   [CaMnter]"
    save = "[App Module]   [Save you from anything]"
}
```

<br>

## execute

<br>
    
```shell
gradle lifePlugin
gradle lifeTask
gradle lifeExtensionTask  
```

<br>
