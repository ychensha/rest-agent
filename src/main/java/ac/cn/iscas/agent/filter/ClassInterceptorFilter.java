package ac.cn.iscas.agent.filter;

import org.springframework.stereotype.Service;

@Service
public class ClassInterceptorFilter implements InterceptorFilter<String>{

  @Override
  public boolean accept(String t) {
    if(t.contains("Welcome")) System.err.println("catch");
    return t.contains("Welcome");
  }

}
