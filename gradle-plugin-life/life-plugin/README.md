# life-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle gradle-plugn-life:life-plugin:uploadArchives 
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
        classpath 'com.camnter.gradle.plugin.life:life-plugin:1.0.3'
    }
}

// sample
lifeExtension {
    id = "[App Module]   [CaMnter]"
    save = "[App Module]   [Save you from anything]"
    nestLifeExtension {
       home = "[App Module]   [https://camnter.com]"
       email = "[App Module]   [yuanyu.camnter@gmail.com]"
    }
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
