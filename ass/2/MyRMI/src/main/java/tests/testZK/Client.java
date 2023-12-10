package tests.testZK;

import RMIZK.Constant;
import RMIZK.ServiceConsumer;
import tests.publicFile.IFoo;

public class Client {
    public static void main(String[] args) throws Exception {
        ServiceConsumer consumer = new ServiceConsumer();

        IFoo foo = consumer.lookup("remoteFoo", Constant.RANDOM_MODE);
//        IFoo foo = consumer.lookup();
        String result = foo.getMessage();
        System.out.println(result);
//        System.gc();
        consumer.close();
    }
}