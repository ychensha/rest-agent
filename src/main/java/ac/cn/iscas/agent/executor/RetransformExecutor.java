package ac.cn.iscas.agent.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import ac.cn.iscas.agent.repo.PointcutRepository.Pointcut;

@Service
public class RetransformExecutor {
  private Executor executor = Executors.newCachedThreadPool();

  public void submit(Pointcut pointcut) {
    executor.execute(new Runnable() {

      @Override
      public void run() {
        Class<?> clazz = null;
        int retry = 0;
        try {
          while ((clazz =
              Class.forName(pointcut.getClassName(), false, pointcut.getLoader())) == null) {
            TimeUnit.SECONDS.sleep(5);
            System.out.println("retry");
            if (++retry == 5)
              break;
          }
        } catch (ClassNotFoundException | InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
