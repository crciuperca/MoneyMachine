package com.crypto.moneymachine.pojo;

public record CurrentBalance(String currency, Double free, Double locked) {
}
//public class CurrentBalance {
//    String currency;
//    Double free;
//    Double locked;
//
//    public CurrentBalance() {
//    }
//
//    public CurrentBalance(String currency, Double free, Double locked) {
//        this.currency = currency;
//        this.free = free;
//        this.locked = locked;
//    }
//
//    public String getCurrency() {
//        return currency;
//    }
//
//    public void setCurrency(String currency) {
//        this.currency = currency;
//    }
//
//    public Double getFree() {
//        return free;
//    }
//
//    public void setFree(Double free) {
//        this.free = free;
//    }
//
//    public Double getLocked() {
//        return locked;
//    }
//
//    public void setLocked(Double locked) {
//        this.locked = locked;
//    }
//}
