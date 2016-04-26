3. 效果展示
==

### 3.1 效果一（验证 Lru，最近没访问的，在溢出时优先被清理）
<img src="http://ww1.sinaimg.cn/large/006lPEc9jw1f36odh8wjdg31401z4u0x.gif" width="320x"/> 
<img src="http://ww4.sinaimg.cn/large/006lPEc9jw1f36p56qcjzj31401z4qa7.jpg" width="320"/>  

**前提：** 设置 LruCache 最大容量为 7MB，把图1、2、3放入了，此时占用容量为：1.87+0.38+2.47=4.47MB。

**执行操作**：
- **1.**然后点 get 图3一共16次（**证明访问次数和Lru没关系，只有访问顺序有关系**）。Recent visit显示了图3
- **2.**先 get 图2，再 get 图1，**制造最近访问顺序为：<1> <2> <3>**
- **3.** put 图4，预算容量需要4.47+2.47=7.19MB。会溢出。
- **4.**溢出了，删除最近没访问的图3。
- **5.**观察 `entryRemoved` 数据 图三被移除了（对照hashcode）

---

### 3.2 效果二（验证 entryRemoved 的 evicted=false，可以验证冲突）
<img src="http://ww3.sinaimg.cn/large/006lPEc9jw1f36oy2uii5g31401z44l6.gif" width="320x"/> <img src="http://ww2.sinaimg.cn/large/006lPEc9jw1f36p6e8t4jj31401z4gt0.jpg" width="320x"/>
  
**前提：**执行了效果一，put 了图4，删除了最近没访问的图3。  

**执行操作**：再一次 put 图4，发生冲突，拿到 key、冲突 value 以及 put 的 value，这里我放到是同一个 hashcode 的 bitmap，所以 hashcode 一样，但是无关紧要吧。