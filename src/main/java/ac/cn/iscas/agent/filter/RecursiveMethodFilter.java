package ac.cn.iscas.agent.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import ac.cn.iscas.agent.filter.RootMethodFilter.Method;

@Service
public class RecursiveMethodFilter implements InterceptorFilter<Method> {
  private Set<Pattern> patterns = new HashSet<>();

  public void add(String pattern) {

  }

  @Override
  public boolean accept(Method t) {
    return false;
  }

}
