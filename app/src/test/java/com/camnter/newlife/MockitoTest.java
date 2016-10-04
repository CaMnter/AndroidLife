package com.camnter.newlife;

import com.camnter.newlife.bean.Contacts;
import com.camnter.newlife.bean.Tag;
import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Description：MockitoTest
 * Created by：CaMnter
 */

public class MockitoTest extends TestCase {

    private static final String MOCK_TEXT = "CaMnter";


    /**
     * 1. 让我们验证某些行为
     * 一旦创建，mock对象会记住所有的交互。然后你可以有选择性的验证你感兴趣的任何交互。
     */
    @SuppressWarnings("unchecked")
    public void test1() {
        System.out.println("\nMockitoTest >>>>>> [test1] >>>>>>");
        // 创建mock
        List mockedList = mock(List.class);

        // 使用 mock 对象
        mockedList.add("one");
        mockedList.clear();

        // 验证
        verify(mockedList).add("one");
        verify(mockedList).clear();
    }


    /**
     * 2. 再来一点 stubbing?
     *
     * 默认情况， 对于返回一个值的所有方法， mock 对象在适当的时候要不返回 null，基本类型/基本类型包装类，或
     * 者一个空集合。比如 int/Integer 返回0, boolean/Boolean 返回 false。
     *
     * 存根 ( stub ) 可以覆盖： 例如通用存根可以固定搭建但是测试方法可以覆盖它。请注意覆盖存根是潜在的代码异
     * 味( code smell )，说明存根太多了
     *
     * 一旦做了存根，方法将总是返回存根的值，无论这个方法被调用多少次
     *
     * 最后一个存根总是更重要 - 当你用同样的参数对同一个方法做了多次存根时。换句话说：存根顺序相关，但是它
     * 只在极少情况下有意义。例如，当需要存根精确的方法调用次数，或者使用参数匹配器等。
     */
    public void test2() {
        System.out.println("\nMockitoTest >>>>>> [test2] >>>>>>");
        // 可以mock具体的类，而不仅仅是接口
        LinkedList mockedList = mock(LinkedList.class);

        // 存根(stubbing)
        when(mockedList.get(0)).thenReturn("first");
        when(mockedList.get(1)).thenThrow(new RuntimeException());

        // 下面会打印 "first"
        System.out.println(mockedList.get(0));

        // 下面会抛出运行时异常
        try {
            System.out.println(mockedList.get(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 下面会打印"null" 因为get(999)没有存根(stub)
        System.out.println(mockedList.get(999));

        // 虽然可以验证一个存根的调用，但通常这是多余的
        // 如果你的代码关心get(0)返回什么，那么有某些东西会出问题(通常在verify()被调用之前)
        // 如果你的代码不关系get(0)返回什么，那么它不需要存根。如果不确信，那么还是验证吧
        verify(mockedList).get(0);
    }


    /**
     * 3. 参数匹配器
     * mockito使用java原生风格来验证参数的值： 使用equals()方法。有些时候，如果需要额外的灵活性，应该使用参数匹配器：
     */
    public void test3() {
        System.out.println("\nMockitoTest >>>>>> [test3] >>>>>>");
        LinkedList mockedList = mock(LinkedList.class);

        //使用内建anyInt()参数匹配器
        when(mockedList.get(anyInt())).thenReturn("element");

        //使用自定义匹配器( 这里的 isValid() 返回自己的匹配器实现 )
        when(mockedList.contains(argThat(null))).thenReturn(false);

        //下面会打印 "element"
        System.out.println(mockedList.get(999));

        // 同样可以用参数匹配器做验证
        verify(mockedList).get(anyInt());
    }


    /**
     * 4. 验证精确调用次数/至少X次/从不
     */
    @SuppressWarnings("unchecked")
    public void test4() {
        System.out.println("\nMockitoTest >>>>>> [test4] >>>>>>");
        LinkedList mockedList = mock(LinkedList.class);
        // 使用 mock
        mockedList.add("once");

        mockedList.add("twice");
        mockedList.add("twice");

        mockedList.add("three times");
        mockedList.add("three times");
        mockedList.add("three times");

        // 下面两个验证是等同的 - 默认使用 times(1)
        verify(mockedList).add("once");
        verify(mockedList, times(1)).add("once");

        // 验证精确调用次数
        verify(mockedList, times(2)).add("twice");
        verify(mockedList, times(3)).add("three times");

        //使用using never()来验证. never()相当于 times(0)
        verify(mockedList, never()).add("never happened");

        //使用 atLeast()/atMost()来验证
        verify(mockedList, atLeastOnce()).add("three times");
        verify(mockedList, atMost(5)).add("three times");
    }


    /**
     * 5. 使用 exception 做 void 方法的存根
     */
    public void test5() {
        System.out.println("\nMockitoTest >>>>>> [test5] >>>>>>");
        LinkedList mockedList = mock(LinkedList.class);
        doThrow(new RuntimeException()).when(mockedList).clear();

        // 下面会抛出 RuntimeException:
        try {
            mockedList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 6. 验证顺序
     */
    @SuppressWarnings("unchecked")
    public void test6() {
        // A. 单个 Mock，方法必须以特定顺序调用
        List singleMock = mock(List.class);

        // 使用单个 Mock
        singleMock.add("was added first");
        singleMock.add("was added second");

        // 为 singleMock 创建 inOrder 检验器
        InOrder inOrder = Mockito.inOrder(singleMock);

        // 下面将确保 add 方法第一次调用是用 "was added first" ,然后是用 "was added second"
        inOrder.verify(singleMock).add("was added first");
        inOrder.verify(singleMock).add("was added second");

        // B. 多个 Mock 必须以特定顺序调用
        List firstMock = mock(List.class);
        List secondMock = mock(List.class);

        // 使用 mock
        firstMock.add("was called first");
        secondMock.add("was called second");

        //创建 inOrder 对象，传递任意多个需要验证顺序的 mock
        inOrder = Mockito.inOrder(firstMock, secondMock);

        // 下面将确保 firstMock 在 secondMock 之前调用
        inOrder.verify(firstMock).add("was called first");
        inOrder.verify(secondMock).add("was called second");

        // Oh, 另外 A + B 可以任意混合
    }


    /**
     * 7. 确保交互从未在mock对象上发生
     */
    @SuppressWarnings("unchecked")
    public void test7() {
        System.out.println("\nMockitoTest >>>>>> [test7] >>>>>>");
        LinkedList mockOne = mock(LinkedList.class);
        LinkedList mockTwo = mock(LinkedList.class);
        LinkedList mockThree = mock(LinkedList.class);

        // 使用 mock - 仅有 mockOne 有交互
        mockOne.add("one");

        // 普通验证
        verify(mockOne).add("one");

        // 验证方法从未在 mock 对象上调用
        verify(mockOne, never()).add("two");

        //验证其他mock没有交互
        verifyZeroInteractions(mockTwo, mockThree);
    }


    /**
     * 8. 发现冗余调用
     * 警告：默写做过很多经典的 expect-run-verify mock 的用户倾向于非常频繁的使用verifyNoMoreInteractions()，
     * 甚至在每个测试方法中。不推荐在每个测试中都使用verifyNoMoreInteractions()。
     * verifyNoMoreInteractions()是交互测试工具集中的便利断言。
     * 仅仅在真的有必要时使用。滥用它会导致定义过度缺乏可维护性的测试。可以在这里找到更多阅读内容。
     * 可以看 never() - 这个更直白并且将意图交代的更好。
     */
    @SuppressWarnings("unchecked")
    public void test8() {
        System.out.println("\nMockitoTest >>>>>> [test8] >>>>>>");
        LinkedList mockedList = mock(LinkedList.class);

        // 使用 mock
        mockedList.add("one");
        mockedList.add("two");

        verify(mockedList).add("one");

        // 下面的验证将会失败
        //verifyNoMoreInteractions(mockedList);
    }


    /**
     * 9. 创建 mock 的捷径 - @mock 注解
     * 最大限度的减少罗嗦的创建mock对象的代码
     * 让测试类更加可读
     * 让验证错误更加可读因为 field name 被用于标志mock对象
     */
    @Mock
    private Contacts contacts;


    public void test9() {
        System.out.println("\nMockitoTest >>>>>> [test9] >>>>>>");
        // 初始化 有Mock 对象
        MockitoAnnotations.initMocks(this);
        // 初始化 没注解的 对象
        Tag tag = mock(Tag.class);
        assertNotNull(tag);
        assertNotNull(this.contacts);
    }


    /**
     * 10. 存根连续调用 ( 游历器风格存根 )
     * 有时我们需要为同一个方法调用返回不同值/异常的存根。
     * 典型使用场景是mock游历器。
     * 早期版本的mockito没有这个特性来改进单一模拟。
     * 例如，为了替代游历器可以使用Iterable或简单集合。
     * 那些可以提供存根的自然方式（例如，使用真实的集合）。在少量场景下存根连续调用是很有用的
     */
    public void testA0() {
        System.out.println("\nMockitoTest >>>>>> [testA0] >>>>>>");
        Tag tag = mock(Tag.class);
        tag.setContent("CaMnter");
        when(tag.getContent())
            .thenThrow(new RuntimeException())
            .thenReturn("thenReturn CaMnter");

        // 第一次调用：抛出运行时异常
        try {
            tag.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 第二次调用: 打印 "thenReturn CaMnter"
        System.out.println(tag.getContent());

        // 任何连续调用: 还是打印 "thenReturn CaMnter" (最后的存根生效).
        System.out.println(tag.getContent());
    }


    /**
     * 11. 带回调的存根
     * 还有另外一种有争议的特性，最初没有包含的mockito中。推荐简单用 thenReturn() 或者 thenThrow() 来做
     * 存根， 这足够用来测试/测试驱动任何干净而简单的代码。然而，如果你对使用一般Answer接口的存根有需要
     */
    public void testA1() {
        System.out.println("\nMockitoTest >>>>>> [testA1] >>>>>>");
        final Tag tag = mock(Tag.class);
        tag.setContent("CaMnter");

        when(tag.getContent()).thenAnswer(new Answer<String>() {
            @Override public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Object mock = invocation.getMock();
                return "called with method: " + invocation.getMethod().getName();
            }
        });
        // 下面会 "called with method: getContent"
        System.out.println(tag.getContent());
    }


    /**
     * 12. doReturn() | doThrow() | doAnswer() | doNothing() | doCallRealMethod() 方法家族
     * 存根void方法需要when(Object)之外的另一个方式，因为编译器不喜欢括号内的void方法......
     * doThrow(Throwable...) 替代 stubVoid(Object) 方法来存根void. 主要原因是改善和doAnswer()方法的可读性和一致性。
     *
     * 可以使用 doThrow(), doAnswer(), doNothing(), doReturn() 和 doCallRealMethod() 代替响应的使用
     * when()的调用， 用于任何方法。下列情况是必须的
     * 1. 存根void方法
     * 2. 在spy对象上存根方法 (看下面)
     * 3. 多次存根相同方法, 在测试中间改变mock的行为
     * 如果喜欢可以用这些方法代替响应的 when()，用于所有存根调用。
     */
    public void testA2() {
        System.out.println("\nMockitoTest >>>>>> [testA2] >>>>>>");
        LinkedList mockedList = mock(LinkedList.class);
        doThrow(new RuntimeException("testA2 RuntimeException")).when(mockedList).clear();
        try {
            mockedList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Spy 与 Mock 的区别:
     * List list = new LinkedList();
     * List spy = spy(list);
     *
     * LinkedList mockedList = mock(LinkedList.class);
     *
     * 结论: spy 模拟和真实创建的对象 List。
     *
     * 可以创建实际对象的间谍 (spy)。当使用 spy 时，真实方法被调用(除非方法被存根)。
     * 只能小心而偶尔的使用 spy，例如处理遗留代码。
     * 在真实对象上做 spy 可以和"部分模拟"的概念关联起来。在1.8版本之前， mockito spy 不是真实的部分模拟。
     * 理由是我们觉得部分 mock 是代码异味。
     */
    @SuppressWarnings("unchecked")
    public void testA3() {
        System.out.println("\nMockitoTest >>>>>> [testA3] >>>>>>");
        List list = new LinkedList();
        List spy = spy(list);

        // 随意的存根某些方法
        when(spy.size()).thenReturn(100);

        // 使用 spy 调用真实方法
        spy.add("one");
        spy.add("two");

        // 打印 "one" - 列表中的第一个元素
        System.out.println(spy.get(0));

        // size() 方法是被存根了的 - 打印100
        System.out.println(spy.size());

        // 随意验证
        verify(spy).add("one");
        verify(spy).add("two");
    }


    /**
     * Spy实际对象时的重要提示！
     *
     * 1. 有时使用when(Object) 来做spy的存根是不可能或者行不通的。在这种情况下使用spy请考虑
     * doReturn|Answer|Throw() 方法家族来做存根。例如：
     * 2. mockito 不会 将调用代理给被传递进去的实际实例，取而代之的是创建它的一个拷贝。因此如
     * 果你持有真实实例并和它交互，不要期待spy会感知到这些交互和实际实例的状态影响。推论是说，当
     * 一个非存根方法在sky上被调用，而不是在真实实例上调用，真实实例不会有任何影响。
     * 3. 对final方法保持警惕。mockito不mock final方法，因此底线是：当你在一个真实对象上 spy + 你想存根
     * 一个final方法 = 问题。同样也无法验证这些方法。
     */
    public void testA4() {
        System.out.println("\nMockitoTest >>>>>> [testA4] >>>>>>");
        List list = new LinkedList();
        List spy = spy(list);

        //不可能: 真实方法被调用因此 spy.get(0) 会抛出IndexOutOfBoundsException (列表现在还是空的)
        //when(spy.get(0)).thenReturn("foo");

        //可以使用 doReturn() 来做存根
        doReturn("foo").when(spy).get(0);
    }


    /**
     * 15. 为进一步断言捕获参数 (Since 1.8.0)
     * mockito 用自然 java 风格验证参数的值：使用 equals() 方法。同样这也是推荐的参数匹配的方式因为它使得
     * 测试干净而简单。但是在某些情况下，在实际验证之后对特定参数做断言是很有用的。
     *
     * 警告：推荐在验证时使用ArgumentCaptor，而不是存根。在存根时使用ArgumentCaptor会减少测试的可读性，因
     * 为捕获器是在断言(验证或者'then')块的外面创建。也可能会降低defect localization(?)因为如果存根方法没
     * 有被调用那么就没有参数被捕获。
     * 某种程度上，ArgumentCaptor 和自定义参数匹配器有关联。这两个技术都被用于确认传递给mock的特定参数。但
     * 是，ArgumentCaptor在下列情况下可能会更适合些：
     */
    public void testA5() {
        System.out.println("\nMockitoTest >>>>>> [testA5] >>>>>>");
        Contacts mock = mock(Contacts.class);
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        mock.setHeader("Y");
        // 参数捕获
        verify(mock).setHeader(argument.capture());
        // 使用 equals 断言
        assertEquals("Y", argument.getValue());
    }

}
