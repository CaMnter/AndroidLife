# toy-time-plugin

<br>
<br>

## generate local plugin
 
<br>
    
```shell
gradle gradle-plugn-life:toy-time-plugin:uploadArchives 
```

<br>

## module build.gradle
 
<br>
    
```gradle
apply plugin: 'com.camnter.gradle.plugin.toytime'

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        // local repository
        maven { url uri('../gradle-plugin-life-repository') }
    }
    dependencies {
        // local repository
        classpath 'com.camnter.gradle.plugin.toytime:toy-time-plugin:1.0.2'
    }
}

// sample
toyTimeExtension {
    // 关键字 可为 空
    keyword = ''
    // 最小记录的 task 运行时间，只显示大于等于改值的 task
    minElapsedMillis = 10
}
```

<br>