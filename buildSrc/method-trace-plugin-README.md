# method-trace-plugin

<br>
<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.method.trace'

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
