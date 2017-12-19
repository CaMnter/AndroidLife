# load-simple-plugin-plugin

<br>
<br>

## generate .class
 
<br>
    
```shell
cd  src/java/com/camnter/load/simple/plugin
javac *.java
```

<br>

## generate jar

```shell
cd  src/java
jar cvf simple-plugin.jar .  
```

<br>

## generate dex
  
```shell
cd  src/java
 dx --dex --output=simple-plugin.dex simple-plugin.jar  
```
   
## issue
   
> bad class file magic or version
   
## Fix

`javac -source 1.6 -target 1.6 *.java`   

or   
   
`javac -source 1.7 -target 1.7 *.java`
   
or   
   
`javac -source 1.8 -target 1.8 *.java`   
       
       
And then regenerating jar and dex.
