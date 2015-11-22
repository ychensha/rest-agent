package ac.cn.iscas.agent.filter;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import ac.cn.iscas.agent.config.AgentConfiguration;
import ac.cn.iscas.agent.filter.RootMethodFilter.Method;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AgentConfiguration.class)
@WebAppConfiguration
public class RootMethodFilterTest {
  @Autowired
  private RootMethodFilter rootFilter;

  private String className = "org/mock/WelcomeServlet";
  private String methodName = "doGet";
  private String desc = "";
  private String signature = "";
  @Test
  public void testAdd() {
    rootFilter.add("java/lang/StringBuilder:");
  }

  @Test
  public void testAccept() {
    testAdd();
    assertTrue(rootFilter.accept(Method.of("append",
        "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", "", "java/lang/StringBuilder")));
  }

  @Test
  public void testMethodToString() {
    System.out.println(Method.of(methodName, desc, signature, className));
  }
}
