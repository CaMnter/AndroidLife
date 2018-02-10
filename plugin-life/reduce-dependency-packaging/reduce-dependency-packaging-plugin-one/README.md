# reduce-dependency-packaging-plugin-one

<br>
<br>

# build host module

<br>

```shell
gradle :plugin-life:reduce-dependency-packaging:reduce-dependency-packaging-plugin-host:build
```

```groovy
/**
 * 必须先编译好宿主项目，在 build/reduceDependencyPackagingHost 下必须存在
 * allVersions.txt
 * Host_R.txt
 * versions.txt
 * */
apply plugin: 'com.camnter.gradle.plugin.reduce.dependency.packaging.plugin'
reduceDependencyPackagingExtension {
    // the package id of Resources.
    packageId = 0x71
    // the path of application module in host project.
    targetHost = '../reduce-dependency-packaging-plugin-host'
    // optional, default value: true.
    applyHostMapping = true
}
```

<br>

## generate .apk
 
<br>

**Build** - **Build Project**
    
`build/output/apk`

Rename ***.apk** to **reduce-dependency-packaging-one.apk** .

<br>

## set plugin apk

**reduce-dependency-packaging-one.apk** add to **assets** of **reduce-dependency-packaging-plugin-host module**.
