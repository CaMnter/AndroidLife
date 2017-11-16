# method-trace-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle gradle-plugin-life:method-trace-plugin:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.method.trace'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        // local repository
        maven { url uri('../gradle-plugin-life/repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin.life:method-trace-plugin:1.1.4'
    }
}

// sample
methodTraceExtension {
    packageName = 'com.camnter'
}
```

<br>

## execute

<br>
    
```shell
gradle methodWholeTraceTask
gradle methodExpectTraceTask
// sample
gradle methodExpectTraceTask -P packageName=com.camnter
```

<br>
