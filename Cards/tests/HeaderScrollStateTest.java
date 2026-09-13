package com.eas.cards2;
public final class HeaderScrollStateTest {
    static int checks;
    static void check(float actual, float expected) { checks++; if (Math.abs(actual-expected)>.0001) throw new AssertionError(actual+" != "+expected); }
    public static void main(String[] args) {
        check(HeaderScrollState.fromRows(0,null,true,-1,true,72),0);
        check(HeaderScrollState.fromRows(0,null,true,3,true,72),0);
        check(HeaderScrollState.fromRows(.4f,null,true,2,true,72),.4f);
        check(HeaderScrollState.fromRows(.4f,-8,false,0,false,72),0);
        check(HeaderScrollState.fromRows(0,36,true,0,false,72),.5f);
        check(HeaderScrollState.fromRows(0,null,true,4,false,72),1);
        check(HeaderScrollState.duringNavigation(0,0,true),0);
        check(HeaderScrollState.duringNavigation(0,.5f,true),0);
        check(HeaderScrollState.duringNavigation(.5f,0,true),.5f);
        check(HeaderScrollState.duringNavigation(.5f,.5f,true),.25f);
        check(HeaderScrollState.duringNavigation(.5f,1,true),0);
        check(HeaderScrollState.duringNavigation(.5f,0,false),0);
        System.out.println("Header scroll: "+checks+" checks passed");
    }
}
