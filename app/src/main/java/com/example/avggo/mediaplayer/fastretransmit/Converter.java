package com.example.avggo.mediaplayer.fastretransmit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by patricktobias on 3/31/16.
 */
public class Converter {

    public static byte[] toBytes (Object obj) throws IOException {
        ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOStream = new ObjectOutputStream(byteOStream);

        objectOStream.writeObject(obj);

        return byteOStream.toByteArray();
    }

    public static Object toObject (byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectOStream = new ObjectInputStream(byteIStream);

        return objectOStream.readObject();
    }
}
