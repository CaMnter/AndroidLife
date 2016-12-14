#include <iostream>
#include <sstream>

using std::string;
using std::stringstream;
using std::cout;
using std::endl;

/**
 *
 * next[j] =
 *
 * 1. 0 , 当 j=1 时
 * 2. Max{ k | 1<k<j , 且 p(1) ... p(k-1) = p(j-k+1) ... p(j-1) }
 * 3. 1 , 其他情况
 *
 * @param s  主串
 * @param next next 数组
 */
void get_next(string s, int *next) {
    int i, j;
    i = 1;
    j = 0;
    next[1] = 0;
    while (i < s.length()) {
        /**
         * s[i] - 后缀的单个字符
         * s[j] - 前缀的单个字符
         */
        if (j == 0 || s[i] == s[j]) {
            ++i;
            ++j;
            next[i] = j;
        } else {
            // 字符不相同,则 j 值回溯
            j = next[j];
        }
    }
}

stringstream build_stringstream(string s, int *next) {
    stringstream ss;
    for (int k = 1; k < s.length(); ++k) {
        ss << next[k]
           << ", ";
    }
    return ss;
}


int index_kmp(string s, string t, int pos) {
    // i 用于主串 s 当前位置下标值, 若 pos 不为 1, 则从 pos 位置开始匹配
    int i = pos;
    // j 用于子串 t 中当前位置下标值
    int j = 1;
    // 定义一个 next 数组
    int next[255];
    // 对子串 t 作分析,得到 next 数组
    get_next(t, next);
    // 若 i 小于 s 的长度且 j 小于 t 的长度时, 循环继续
    while (i <= s.length() && j <= t.length()) {
        // 两字母相等则继续,与朴素算法增加, 也把 j==0 判断了
        if (j == 0 || s[i] == t[j]) {
            i++;
            j++;
        } else {
            // 子串指针后退重新开始匹配
            // j 根据分析出 next 数组, 回退到合适的位置, i值不变
            j = next[j];
        }
    }
    if (j > t.length()) {
        return (int) (i - t.length());
    } else {
        return 0;
    }
}


int main() {

    /*************
     * Test Next *
     *************/

    // next = 011234223
    const string s = " ababaaaba";
    int next[10];
    get_next(s, next);
    cout << " >>>>>>> next test <<<<<< \ns = " + s + " \n"
         << build_stringstream(s, next).str() + "\n"
         << endl;

    /*************
     * Test KMP *
     *************/

    const string s1 = "ababaaaba";
    const string t = " aaaba";
    cout << " >>>>>>> kmp test <<<<<< \n"
         << "s = "
         << s1 + "\n"
         << "t = \"aaab\"  \n"
         << "index = "
         << index_kmp(s1, t, 1)
         << "\n"
         << endl;

    return 0;

}

