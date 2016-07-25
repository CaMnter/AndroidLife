package com.camnter.effective.test;

/**
 * Description：Student
 * Created by：CaMnter
 */

public class Student {

    int number = 0;
    String name = "";


    public void setNumber(int n) {
        synchronized (name) {
            this.number = n;
        }
    }


    public void setName(String s) {
        synchronized (name) {
            this.name = s;
        }
    }
}
