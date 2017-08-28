# gradle-plugin-life

<br>
<br>

## generate local plugin
 
<br>
    
```{r, engine='shell', count_lines}
gradle :gradle-plugin-life:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```{r, engine='groovy', count_lines}
apply plugin: 'com.android.application'
apply plugin: 'com.camnter.gradle.plugin.life'

buildscript {
    repositories {
    
        ...
        
        jcenter()
        mavenLocal()
        maven { url uri('../gradle-plugin-life/repository') }
        
    }
    dependencies {
    
        ...
        
        // local repository
        classpath 'com.camnter.gradle.plugin.life:gradle-plugin-life:1.0.0'
        
        ...
        
    }
}
```

<br>

## execute

<br>
    
```{r, engine='shell', count_lines}
gradle camnterLifeTask
```

<br>
