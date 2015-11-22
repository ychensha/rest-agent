package ac.cn.iscas.agent.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Service
public class RootMethodFilter implements InterceptorFilter<RootMethodFilter.Method> {
  private static final String CLAZZ_METHOD_SPLIT = ":";
  private Set<Pattern> patterns = new HashSet<>();


  private RootMethodFilter() {
    add("org/mock/servlet/WelcomeServlet:doGet");
  }

  public void add(String pattern) {
    pattern = pattern.replace(" ", "");
    if (pattern.indexOf(CLAZZ_METHOD_SPLIT) == -1)
      throw new IllegalArgumentException("wrong filter expression");
    String clazz = pattern.substring(0, pattern.lastIndexOf(CLAZZ_METHOD_SPLIT));
    String[] methods =
        pattern.substring(pattern.indexOf(CLAZZ_METHOD_SPLIT) + 1, pattern.length()).split(",");
    if (methods.length == 0 || "".equals(methods[0]))
      patterns.add(Pattern.compile(clazz + ".*"));
    else {
      for (String method : methods)
        patterns.add(Pattern.compile(clazz + CLAZZ_METHOD_SPLIT + method));
    }
  }

  @Override
  public boolean accept(Method t) {
    for (Pattern p : patterns) {
      if (p.matcher(t.getOwner() + CLAZZ_METHOD_SPLIT + t.getName()).matches())
        return true;
    }
    return false;
  }


  /**
   * Unreachable means dead code, Static means there is a invokeStatic associated with this method,
   * Virtual means this method has been override in subclass, Scanned means the method has been
   * processed.
   * 
   * @author yuanxinchen
   *
   */
  public static enum AnalysisState {
    Unreachable, Static, Special, Virtual, Scanned
  }

  @ToString
  @EqualsAndHashCode
  public static class Method {
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private String name;
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private String desc;
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private String signature;
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private String owner;

    private Method() {}

    public static Method of(String name, String desc, String signature, String owner) {
      Method ret = new Method();
      ret.setName(name);
      ret.setDesc(desc);
      ret.setSignature(signature);
      ret.setOwner(owner);
      return ret;
    }
  }
}
