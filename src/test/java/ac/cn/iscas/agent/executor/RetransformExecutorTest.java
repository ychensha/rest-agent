package ac.cn.iscas.agent.executor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import ac.cn.iscas.agent.config.AgentConfiguration;
import ac.cn.iscas.agent.repo.PointcutRepository.Pointcut;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AgentConfiguration.class)
@WebAppConfiguration
public class RetransformExecutorTest {

  @Autowired
  private RetransformExecutor executor;
  
  @Test
  public void testSubmit() {
    executor.submit(Pointcut.of("hahahah", "hahahah", getClass().getClassLoader()));
  }

}
