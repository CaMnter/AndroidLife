# gradle-plugin-life

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle :gradle-plugin-life:uploadArchives 
```

<br>

## project build.gradle
 
<br>
    
```gradle
buildscript {
    repositories {
    
        ...
        
        maven { url uri('../gradle-plugin-life-repository') }
        
        ...
        
    }
    dependencies {
    
        ...
        
        // local repository
        classpath 'com.camnter.gradle.plugin.life:gradle-plugin-life:1.0.1'
        
        ...
        
    }
}
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.life'
```

<br>

## execute

<br>
    
```shell
gradle lifePlugin
gradle lifeTask
```

<br>
