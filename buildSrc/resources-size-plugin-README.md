# resources-size-plugin

<br>
<br>


<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.resources.size'

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
