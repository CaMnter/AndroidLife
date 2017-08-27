# gradle-plugin-life

<br>
<br>

## generate local plugin
 
<br>
    
```{r, engine='shell', count_lines}
gradle :gradle-plugin-life:uploadArchives 
```

<br>

## project build.gradle
 
<br>
    
```{r, engine='groovy', count_lines}
buildscript {
    repositories {
    
        ...
        
        maven {
            url uri('gradle-plugin-life/repository')
        }
        
    }
    dependencies {
    
        ...
        
        // local repository
        classpath 'com.camnter.plugin.life:gradle-plugin-life:1.0.0'
        
        ...
        
    }
}
```

<br>

## module build.gradle
 
<br>
    
```{r, engine='groovy', count_lines}
apply plugin: 'com.camnter.plugin.life'
```

<br>

## execute

<br>
    
```{r, engine='shell', count_lines}
gradle LifeTask
```

<br>
