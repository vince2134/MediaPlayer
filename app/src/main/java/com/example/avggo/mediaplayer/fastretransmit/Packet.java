package com.example.avggo.mediaplayer.fastretransmit;

import java.util.Arrays;

/**
 * Created by patricktobias on 3/31/16.
 */
public class Packet {

    public int seqNo;
    public byte[] data;
    public boolean last;

    public Packet (int seqNo, byte[] data, boolean last) {
        this.seqNo = seqNo;
        this.data = data;
        this.last = last;
    }

    public int getSeqNo () {
        return seqNo;
    }

    public byte[] getData () {
        return data;
    }

    public boolean isLast () {
        return last;
    }

    public void setSeqNo (int seqNo) {
        this.seqNo = seqNo;
    }

    public void setData (byte[] data) {
        this.data = data;
    }

    public void setLast (boolean last) {
        this.last = last;
    }

    @Override
    public String toString() {
        return "Packet with seqNo = " + seqNo + ", data = " + Arrays.toString(data) + ", last = " + last;
    }
}
