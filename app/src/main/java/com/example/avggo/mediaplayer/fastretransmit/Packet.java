package com.example.avggo.mediaplayer.fastretransmit;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by patricktobias on 3/31/16.
 */
public class Packet implements Serializable {

    private int seqNo;
    private byte[] data;
    private boolean last;

    public Packet (int seqNo, byte[] data, boolean last) {
        super();
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

    public void setLast (boolean alst) {
        this.last = last;
        //hoho
    }

    @Override
    public String toString() {
        return "Packet { seq = " + seqNo +", data = " + Arrays.toString(data) + ", last = " + last + "}";
    }
}
