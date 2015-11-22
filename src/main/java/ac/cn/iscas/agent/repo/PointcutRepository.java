package ac.cn.iscas.agent.repo;


import java.lang.instrument.Instrumentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ac.cn.iscas.agent.Agent;
import ac.cn.iscas.agent.filter.ClassInterceptorFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Service
@EqualsAndHashCode(callSuper=false)
public class PointcutRepository extends AbstractRepository<Class<?>, PointcutRepository.Pointcut> {
  public static class Pointcut {
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private String className;
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private String methodName;
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private ClassLoader loader;

    public static Pointcut of(String className, String methodName, ClassLoader loader) {
      Pointcut ret = new Pointcut();
      ret.className = normalizeClass(className);
      ret.methodName = normalizeMethod(methodName);
      ret.loader = loader;
      return ret;
    }

    private static String normalizeClass(String className) {
      return className.replace('/', '.');
    }

    private static String normalizeMethod(String methodName) {
      return methodName;
    }

    @Override
    public String toString() {
      return new StringBuilder().append('[').append(loader).append(":").append(className)
          .append("::").append(methodName).append(']').toString();
    }
  }

  private Instrumentation inst;
  @Autowired
  private ClassInterceptorFilter classFilter;

  private PointcutRepository() {
    map.put(Pointcut.of("org.mock.servlet.WelcomeServlet", "doGet", getClass().getClassLoader()),
        this.getClass());
    inst = Agent.inst;
  }

  public Instrumentation getInst() {
    return inst;
  }

  public void setInst(Instrumentation inst) {
    this.inst = inst;
  }

  @Override
  public void add(Pointcut pointcut, Class<?> clazz) {
    try {
      if (clazz == null)
        clazz = getClass();
      classFilter.add(pointcut.getClassName());
      map.put(pointcut, clazz);
      Class<?> toRedefine = Class.forName(pointcut.getClassName());
      inst.retransformClasses(toRedefine);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
