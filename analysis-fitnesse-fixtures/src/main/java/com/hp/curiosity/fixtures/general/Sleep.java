package com.hp.curiosity.fixtures.general;

public class Sleep {

    private Long time;

    public void setTime(Long time) throws IllegalArgumentException {
        if (time <= 0L) {
            throw new IllegalArgumentException("positive value required");
        } else {
            this.time = time;
        }
    }

    public Boolean start() throws InterruptedException {

        Thread.sleep(time);
        //return true when sleep period concluded
        return true;
    }
}
