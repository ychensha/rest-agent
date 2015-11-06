package ac.cn.bytebuddy;

import static org.junit.Assert.*;

import org.junit.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;

public class ByteBuddyExample {

  @Test
  public void byteBuddyInstance() {
    ByteBuddy instance = new ByteBuddy();
    instance.redefine(this.getClass());
    instance.subclass(this.getClass());
    instance.rebase(this.getClass());
  }

  @Test
  public void makeClass() throws InstantiationException, IllegalAccessException {
    Class<?> clazz = getInstance().subclass(Object.class).name("example.Type")
        .method(ElementMatchers.named("toString")).intercept(FixedValue.value(0))
        .make().load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
    System.out.println(clazz.getName());
    System.out.println(clazz.getClassLoader());
    System.out.println(getClass().getClassLoader());
    assertEquals(clazz.getClassLoader(), getClass().getClassLoader());
    String toString = clazz.newInstance().toString();
    System.out.println();
    System.out.println(toString);
  }

  private ByteBuddy getInstance() {
    return new ByteBuddy();
  }
}
