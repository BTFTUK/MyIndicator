package com.btftu.myindicator.indicator;


public class CirclePoint {

    public float x;//圆心坐标x
    public float y;//圆心坐标y
    private float radius;//半径
    private int num;//排序号
    private int distance;//距离中心的距离
    private int state;//0静止 1远离 2靠近

    public static final int CLOSE_TO_CENTER = 2;
    public static final int KEEP_AWAY_CENTER = 1;
    public static final int STATIC_AUTOCHTHONOUS = 0;

    public CirclePoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
