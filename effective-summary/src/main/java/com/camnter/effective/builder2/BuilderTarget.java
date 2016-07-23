package com.camnter.effective.builder2;

/**
 * Description：2 - BuilderTarget
 * Created by：CaMnter
 */

public class BuilderTarget {

    private final int a;
    private final int b;
    private final int c;
    private final int d;
    private final int e;


    public static class Builder {
        // Required parameters
        private final int a;
        private final int b;

        // Optional parameters - initialized to default values
        private int c = 0;
        private int d = 0;
        private int e = 0;


        public Builder(int a, int b) {
            this.a = a;
            this.b = b;
        }


        public Builder setC(int c) {
            this.c = c;
            return this;
        }


        public Builder setD(int d) {
            this.d = d;
            return this;
        }


        public Builder setE(int e) {
            this.e = e;
            return this;
        }


        public BuilderTarget build() {
            return new BuilderTarget(this);
        }
    }


    private BuilderTarget(Builder builder) {
        this.a = builder.a;
        this.b = builder.b;
        this.c = builder.c;
        this.d = builder.d;
        this.e = builder.e;
    }


    public static void main(String[] args) {
        BuilderTarget builderTarget = new BuilderTarget
            .Builder(16, 17)
            .setC(18)
            .setD(19)
            .setE(20)
            .build();
        System.out.println(
            builderTarget.a + ", " + builderTarget.b + ", " + builderTarget.c + ", " +
                builderTarget.d + ", " + builderTarget.e);
    }

}
