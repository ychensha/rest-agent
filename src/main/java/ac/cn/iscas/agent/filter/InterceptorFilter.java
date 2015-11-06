package ac.cn.iscas.agent.filter;

public interface InterceptorFilter<T> {
  boolean accept(T t);
}
