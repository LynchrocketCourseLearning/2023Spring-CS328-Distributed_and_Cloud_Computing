package tests.testZK;

import RMIZK.Constant;
import RMIZK.ServiceConsumer;
import tests.publicFile.IFoo;

public class Client2 {
    public static void main(String[] args) throws Exception {
        ServiceConsumer consumer = new ServiceConsumer();

        IFoo foo = consumer.lookup("remoteFoo2", Constant.LEAST_CONN_MODE);
        String result = foo.getMessage();
        System.out.println(result);
        consumer.close();
    }
}