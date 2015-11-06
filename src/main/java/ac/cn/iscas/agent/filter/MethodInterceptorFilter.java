package ac.cn.iscas.agent.filter;

import org.springframework.stereotype.Service;

@Service
public class MethodInterceptorFilter implements InterceptorFilter<MethodInterceptorFilter.Method> {
  public static class Method {
    private String name;
    private String desc;
    private String signature;

    private Method() {}

    public static Method of(String name, String desc, String signature) {
      Method ret = new Method();
      ret.setName(name);
      ret.setDesc(desc);
      ret.setSignature(signature);
      return ret;
    }

    public String getName() {
      return name;
    }

    protected void setName(String name) {
      this.name = name;
    }

    public String getDesc() {
      return desc;
    }

    protected void setDesc(String desc) {
      this.desc = desc;
    }

    public String getSignature() {
      return signature;
    }

    protected void setSignature(String signature) {
      this.signature = signature;
    }
  }

  @Override
  public boolean accept(Method t) {
    return true;
  }

}
