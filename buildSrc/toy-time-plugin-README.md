# toy-time-plugin

<br>
<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.toytime'

// sample
toyTimeExtension {
    // 关键字 可为 空
    keyword = ''
    // 最小记录的 task 运行时间，只显示大于等于改值的 task
    minElapsedMillis = 10
}
```

<br>