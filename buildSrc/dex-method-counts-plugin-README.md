# dex-method-counts-plugin

<br>
<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.dex.method.counts'

// sample
dexMethodCountsExtension {
    countFields = false
    includeClasses = false
    printAble = true
    packageFilter = ""
}
```

<br>

## execute

<br>
    
```shell
gradle aD
gradle aR
gradle build
```

<br>
