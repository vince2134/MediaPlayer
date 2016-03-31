package com.example.avggo.mediaplayer.fastretransmit;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by patricktobias on 3/31/16.
 */
public class Packet implements Serializable{

    private int seqNo;
    private byte[] data;

    public Packet (int seqNo, byte[] data) {
        super();
        this.seqNo = seqNo;
        this.data = data;
    }

    public int getSeqNo () {
        return seqNo;
    }

    public byte[] getData () {
        return data;
    }

    public void setSeqNo (int seqNo) {
        this.seqNo = seqNo;
    }

    public void setData (byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Packet { seq = " + seqNo +", data = " + Arrays.toString(data) + "}";
    }
}
