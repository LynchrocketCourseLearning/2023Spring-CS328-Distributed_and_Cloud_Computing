package tests.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MIReflect {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Hello obj = new Hello();
        String methodName = "invokeMe";
        Object[] arg = {"id", new Foo()};
        Class<?>[] argTypes = {String.class, Foo.class};

        // error: NoSuchMethodException
//        Method method = obj.getClass().getMethod(methodName, argTypes);
//        method.invoke(obj, arg);

        // correct
        Object result = null;
        Method[] methods = obj.getClass().getMethods();
        boolean foundMethod = false;
        for (Method m : methods) { // iterate all public methods
            if (m.getName().equals(methodName)) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                int n = parameterTypes.length;
                if (n != argTypes.length) continue;
                boolean flag = false;
                for (int i = 0; i < n; i++) {
                    if (!parameterTypes[i].isAssignableFrom(argTypes[i])) { // check if assignable
                        flag = true;
                        break;
                    }
                }
                if (flag) continue;             // not assignable then next
                result = m.invoke(obj, arg);   // else invoke
                foundMethod = true;
            }
        }
    }
}

interface IFoo{/*...*/}

class Foo implements IFoo{/*...*/}

class Hello {
    public void invokeMe(String id, IFoo ifoo){System.out.println("in invokeMe(String id, IFoo ifoo)");}
    public void invokeMe(String fake){System.out.println("in invokeMe(String fake)");}
    public void invokeThou(String id, IFoo ifoo){System.out.println("invokeThou(String id, IFoo ifoo)");}
}