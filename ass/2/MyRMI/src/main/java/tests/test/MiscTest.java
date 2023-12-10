package tests.test;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class MiscTest {
    public static void main(String[] args) throws URISyntaxException {
        // null test
//        byte[] serialize = SerializationUtils.serialize((Serializable) null);
//        Object deserialize = SerializationUtils.deserialize(serialize);
//        System.out.println(Arrays.toString(serialize));
//        System.out.println(deserialize);
//        System.out.println(deserialize == null);

        String data = "rmi://localhost:2000/remoteFoo/ds";
        String[] split = data.split("/");
        System.out.println(Arrays.toString(split));
    }
}
