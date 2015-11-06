package ac.cn.iscas.agent.repo;


import org.springframework.stereotype.Service;

@Service
public class PointcutRepository extends AbstractRepository<Class<?>, PointcutRepository.Pointcut> {
  public static class Pointcut {
    private String className;
    private String methodName;

    public static Pointcut of(String className, String methodName) {
      Pointcut ret = new Pointcut();
      ret.className = normalizeClass(className);
      ret.methodName = normalizeMethod(methodName);
      return ret;
    }

    private static String normalizeClass(String className) {
      return className.replace('/', '.');
    }

    private static String normalizeMethod(String methodName) {
      return methodName;
    }

    public String getClassName() {
      return className;
    }

    protected void setClassName(String className) {
      this.className = className;
    }

    public String getMethodName() {
      return methodName;
    }

    protected void setMethodName(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Pointcut other = (Pointcut) obj;
      if (className == null) {
        if (other.className != null)
          return false;
      } else if (!className.equals(other.className))
        return false;
      if (methodName == null) {
        if (other.methodName != null)
          return false;
      } else if (!methodName.equals(other.methodName))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return new StringBuilder().append('[').append(className).append("::").append(methodName)
          .append(']').toString();
    }
  }

  private PointcutRepository() {
    map.put(Pointcut.of("org.mock.servlet.WelcomeServlet", "doGet"), this.getClass());
  }
}
