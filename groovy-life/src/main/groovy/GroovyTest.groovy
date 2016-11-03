println "\n 1. <Hello World by Groovy?"


println "\n 2. <变量标识符>"
def sign = "Miss"
def target = 26
println sign + " " + target


println "\n 3. <引用标识符>"
def map = [:]
map."22" = "2233"
map."save" = "Save you from anything"
println "22 == " + map."22" + "\nsave == " + map."save"
// 都是对的
map.'jud'
map."you"
map.'''from'''
map."""anything"""
map./save you/
map.$/tes/$
// 特殊的 GString，也是对的
def firstName = "26"
map."CaMnter-${firstName}" = "CaMnter-26"
assert map.'CaMnter-26' == "CaMnter-26"


println "\n 4. <单引号字符串>：不支持占位符"
// $sign 不会被替换
def stringHolder = '$sign you'
println stringHolder


println "\n 5. <三单引号字符串>"
def multiLineString = '''Save
you
from
anything'''
println multiLineString


println "\n 6. <双引号字符串>：支持插值，如果双引号字符串中不包含站位符则是 java.lang.String 类型的，如果双引号字符串中包含站位符则是 groovy.lang.GString 类型的"
// 插值占位符可以用 ${} 或者 $ 来标示，${} 用于一般替代字串或者表达式，$ 主要用于 A.B 的形式中
def you = "you"
def saveHolder = "save ${you} from anything"
println saveHolder

def sumHolder = "2 + 2 + 2 = ${2 + 2 + 2}"
println sumHolder

// 特别注意，$ 只对 A.B 等有效，如果表达式包含括号（像方法调用）、大括号、闭包等符号则是无效的。
def apple = [price: 26, color: 'red']
println "$apple.color apple price == $apple.price"

// 无参数闭包
def parameterLessClosure = "1 + 2 == ${-> 3}"
println parameterLessClosure
def oneParamClosure = "1 + 2 == ${w -> w << 3}"
println oneParamClosure

def magicNumber = 6
def eagerGString = "magicNumber == ${magicNumber}"
// 闭包造成延迟加载
def lazyGString = "magicNumber == ${-> magicNumber}"
println "eagerGString == ${eagerGString}"
println "lazyGString == ${lazyGString}"
magicNumber = 26
println "eagerGString == ${eagerGString}"
// 一个普通插值表达式值替换实际是在 GString 创建的时刻，一个包含闭包的表达式由于延迟运算调运 toString() 方法，所以会产生一个新的字符串值。
println "lazyGString == ${lazyGString}"


println "\n 7. <多重双引号字符串>"
def multiDoubleQuotesString = """
        Dear 6

        Save you from anything
                       CaMnter
"""
println multiDoubleQuotesString


println "\n 8. <斜线字符串>：双引号字符串很类似，通常用在正则表达式中"
def savePattern = /.*save.*/
println savePattern
// 有转义
def escapeSlash = / Save you \/ from anything/
println escapeSlash
def multiSlashString = /
Save
you
from
anything/
println multiSlashString
def interpolatedSlash = /${savePattern} you from anything/
println interpolatedSlash


println "\n 9. <字符 Characters>"
char c1 = 'S'
def c2 = 'a' as Character
def c3 = (char) 'v'
def c4 = (char) 'e'
println c1 + c2 + c3 + c4


println "\n 10. <数值>"
byte b = 26
char c = 26
short s = 26
int i = 26
long l = 26
float f = 2.666
double d = 2.666
BigInteger bi = 26
// 八进制
int xInt = 077
println "077 八进制 == " + xInt
// 十六进制
xInt = 0x77
println "0x77 十六进制 == " + xInt
// 二进制
xInt = 0b11
println "0x77 二进制 == " + xInt


println "\n 11. <Boolean>"
def booleanValue = true
assert booleanValue


println "\n 12. <List>"
def list = [true, '2', 6, 6, 6, 6]
assert list.size() == 6
assert list instanceof List
// 默认类型为 ArrayList
assert list instanceof ArrayList
// 强转
def linkedList = [2, 6, 6] as LinkedList
assert linkedList instanceof LinkedList
// 指定类型
LinkedList nextLinkedList = [2, 6, 6]
assert nextLinkedList instanceof LinkedList
// 左边开始
assert list[0] == true
assert list[1] == '2'
// 右边开始
assert list[-6] == true
assert list[-5] == '2'
// 添加 item
nextLinkedList << "2"
assert nextLinkedList.size() == 4
// 子集
assert list[0, 3] == [true, 6]
assert list[0..3] == [true, '2', 6, 6]
// 多维数组
def multiArray = [[2, 2], [6, 6]];
assert multiArray[0][1] == 2


println "\n 13. <数组>"
String[] stringArray = ['Save', 'You']
assert stringArray instanceof String[]
// def 定义 array
def defArray = [2, 6] as int[]
assert defArray instanceof int[]
// 多维数组
def matrixArray = new Integer[6][6]
assert matrixArray.size() == 6


println "\n 14. <Map>"
def colorMap = [white: '#FFFFFFFF', black: '#FF000000']
// 默认类型 LinkedHashMap
assert colorMap instanceof LinkedHashMap
assert colorMap.unknown == null
def mapKey = 'oneKey'
// 变量作为 key ，不可行
def personMap = [mapKey: "You name"]
assert !personMap.containsKey('oneKey')
// 变量作为 key ，加 () ，可行
personMap = [(mapKey): "You name"]
assert personMap.containsKey('oneKey')


println "\n 15. <运算符>"
// 次方运算符
assert 2**3 == 8
// !
println "(!'save') == " + (!'save')
println "(!'') == " + (!'')
// .@：Groovy 支持.@ 直接域访问操作符
class Color {
    public final String value

    Color(String value) {
        this.value = value
    }

    String getValue() {
        "value: $value"
    }
}

def colorObject = new Color('#ffffffff')
println 'colorObject.value == ' + colorObject.value
println 'colorObject.@value == ' + colorObject.@value
// Groovy 支持 .& 方法指针操作符，因为闭包可以被作为一个方法的参数，如果想让一个方法作为另一个方法的参数则
// 可以将一个方法当成一个闭包作为另一个方法的参数
// 遍历 List
list.each {
    println(it)
}

String printValue(value) {
    println value
}

// 方法指针操作符写法
list.each(this.&printValue)
// Groovy 支持将 ?: 三目运算符简化为二目
// colorObject.@value ? colorObject.@value : '????' >> colorObject.@value ?: '????'
assert colorObject.@value == colorObject.@value ?: '??????'
// Groovy支持 *. 展开运算符，一个集合使用展开运算符可以得到一个元素为原集合各个元素执行后面指定方法所得值的集合
colors = [new Color('#ffffffff'),
          new Color('#ff000000'),
          null]
assert (colors*.value as String).equals('[value: #ffffffff, value: #ff000000, null]')
assert (colors*.@value as String).equals('[#ffffffff, #ff000000, null]')


println "\n 16. <Import>"

import groovy.io.FileType
import groovy.io.FileVisitResult
import groovy.xml.MarkupBuilder

import static Boolean.FALSE
import static java.util.Calendar.getInstance as cInstance

def xml = new MarkupBuilder()
assert xml != null

assert !FALSE
// 取别名
assert cInstance().class == Calendar.getInstance().class

def printShe() {
    println GroovyField.she
}

printShe()


println "\n 17. <闭包>"
def magicI = 0x00;
// 基本
def baseClosure = { 0x00 }
// —> 分割参数
def arrowClosure = { -> magicI++ }
// 隐含参数 it
def hideArgItClosure = { it -> println it }
// 显示参数
def argsClosure = { magic -> println magic }
// 两参数
def twoArgsClosure = { String what, int tag -> println what + tag }
twoArgsClosure == { String what, int tag ->
    def tempTag = tag >> 2 >> 2
    println what + tempTag
}
// 闭包为 Closure 类型
assert twoArgsClosure instanceof Closure
Closure<String> getFileName = { File it -> it.name }
// 调用
assert baseClosure() == 0x00
assert baseClosure.call() == 0x00
// 如果闭包没定义参数，默认隐含一个名为 it 的参数
def defaultItArgClosure = { it }
assert defaultItArgClosure() == null
assert defaultItArgClosure.call(26) == 26
baseClosure = { "Save you: $it" }
assert baseClosure(26) as String == 'Save you: 26'

def joioOne = { String... args -> args.join('') }
assert joioOne('Save', 'You') == 'SaveYou'
def joinTwo = { String[] args -> args.join('') }
assert joinTwo('Save', 'You') == 'SaveYou'

def multiJoin = { int n, String... args -> args.join('') * n }
assert multiJoin(2, 'Save', 'You') == 'SaveYouSaveYou'

def useClosure(int she, String info, Closure closure) {
    info + closure.call(' you from anything ') + she
}

assert useClosure(6, 'Save', { it }) == 'Save you from anything 6'


println "\n 18. <I/O 操作>"
final def groovyBaseDir = System.getProperty('user.dir')
final def readFileName = 'GroovyField.groovy'
final def readFile = new File(groovyBaseDir, readFileName)
println 'File.eachLine - 1: '
readFile.eachLine { line -> println line }
println 'File.eachLine - 2: '
readFile.eachLine { line, lineNumber -> println "$lineNumber: $line" }
def lineCount = 0, MAX_LINE_COUNT = 2
println '闭包 - File.withReader: '
readFile.withReader { reader ->
    for (; ;) {
        println reader.readLine()
        if (++lineCount > MAX_LINE_COUNT) {
            try {
                throw new RuntimeException("Save you from anything Exception")
            } catch (Exception e) {
                e.printStackTrace()
                break
            }
        }
    }
}
// 读取文件 -> List
def fileList = readFile.collect { it }
// 读取文件 -> Array
def fileArray = readFile as String[]
println 'File >> String[].each: '
fileArray.each {
    println it
}
// 读取文件 -> byte[]
byte[] fileByteArray = readFile.bytes
// 读取文件 -> InputStream -> 1：需要手动关闭流
def input = readFile.newInputStream()
input.close()
// 读取文件 -> InputStream -> 2
// 读取文件 -> InputStream -> 2 闭包：不需要手动关闭流
println '闭包 - File.withInputStream: '
readFile.withInputStream { stream ->
    BufferedInputStream bufferedInputStream = new BufferedInputStream(stream)
    def inputByte = new byte[1024]
    int temp
    while ((temp = bufferedInputStream.read(inputByte, 0, 1024)) != -1) {
        System.out.write(inputByte, 0, temp)
    }
    println ''
}
// 写文件 -> 1
final def writeFileNameOne = 'WriterTempOne.groovy'
final def writeFileOne = new File(groovyBaseDir, writeFileNameOne)
def checkFile = { File file ->
    if (file == null) return
    if (!file.parentFile.exists()) file.mkdirs()
    if (file.exists()) file.delete()
    file.createNewFile()
}
checkFile.call(writeFileOne)
writeFileOne.withWriter('utf-8') {
    writer -> writer.write 'println \'Save you from anything —— Temp one\''
}
// 写文件 -> 2
final def writeFileNameTwo = 'WriterTempTwo.groovy'
final def writeFileTwo = new File(groovyBaseDir, writeFileNameTwo)
checkFile(writeFileTwo)
writeFileTwo << 'println \'Save you from anything —— Temp two\''
// 打印目录所有文件名
println '遍历目录所有文件名：'
writeFileTwo.parentFile.eachFile { file -> println file.name
}
// 正则 打印目录所有文件名
println '正则 遍历目录所有文件名：'
writeFileTwo.parentFile.eachFileMatch(~/Groovy.*\.groovy/) { file -> println file.name }
println '深度 遍历目录所有文件名：'
writeFileTwo.parentFile.parentFile.eachFileRecurse { file -> println file.name }
println '深度 遍历目录所有 file 非 dir：'
writeFileTwo.parentFile.parentFile.eachFileRecurse(FileType.FILES) { file -> println file.name }
println '允许 设置特殊标记规则：'
writeFileTwo.parentFile.parentFile.traverse { file ->
    if (file.directory && file.name == 'groovy') {
        FileVisitResult.TERMINATE
        println file.name
    } else {
        FileVisitResult.CONTINUE
    }
}

// linux 的命令 ls -l
def process = "ls -l".execute()
println "Found file ${process.text}"


println "\n 19. <ConfigSlurper>"
def config = new ConfigSlurper().parse('''
    camnter.date = new Date()
    camnter.sign  = 26
    camnter {
        name = "camnter ${26}"
    }
''')
assert config.camnter.date instanceof Date
assert config.camnter.sign == 26
assert config.camnter.name == 'camnter 26'


println "\n 20. <Expando>"
def expando = new Expando()
expando.toString = { -> 'CaMnter' }
expando.anything = { String anything -> 'Save you from ' + anything }
assert expando as String == 'CaMnter'
assert expando.anything('anything') == 'Save you from anything'