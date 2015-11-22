package ac.cn.iscas.agent.instrumentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import ac.cn.iscas.agent.config.AgentConfiguration;
import ac.cn.iscas.agent.filter.ClassInterceptorFilter;
import ac.cn.iscas.agent.filter.InterceptorFilter;
import ac.cn.iscas.agent.filter.RootMethodFilter;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AgentConfiguration.class)
@WebAppConfiguration
public class BaseMethodAdciceTest {
  @Autowired
  private ApplicationContext context;
  @Autowired
  private ClassInterceptorFilter classFilter;
  @Autowired
  private RootMethodFilter methodFilter;
  
  
  @Test
  public void test() throws FileNotFoundException, IOException {
    ClassReader cr = new ClassReader(new FileInputStream(new File(getClass().getClassLoader().getResource("").getPath() + "WelcomeServlet.class")));
    ClassWriter cw = new ClassWriter(cr, 0);
//    cr.accept(new BaseClassVisitor(methodFilter, cw), 0);
    System.out.println(cw.toByteArray());
  }

}
