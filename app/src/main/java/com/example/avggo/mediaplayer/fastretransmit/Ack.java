package com.example.avggo.mediaplayer.fastretransmit;

import java.io.Serializable;

/**
 * Created by patricktobias on 3/31/16.
 */
public class Ack implements Serializable {
    private int packetNo;

    public Ack (int packetNo) {
        super();
        this.packetNo = packetNo;
    }

    public int getPacketNo () {
        return packetNo;
    }

    public void setPacketNo(int packetNo) {
        this.packetNo = packetNo;
    }
}
