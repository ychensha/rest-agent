package ac.cn.iscas.agent.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class ClassInterceptorFilter implements InterceptorFilter<String> {
  private Set<Pattern> patterns = new HashSet<>();

  private ClassInterceptorFilter() {
    add("org.mock.servlet.*");
  }

  @Override
  public boolean accept(String t) {
    for (Pattern pattern : patterns) {
      if (pattern.matcher(t).matches())
        return true;
    }
    return false;
  }

  public void add(String pattern) {
    patterns.add(Pattern.compile(pattern));
  }
}
