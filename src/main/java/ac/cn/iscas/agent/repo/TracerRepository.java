package ac.cn.iscas.agent.repo;


import org.springframework.stereotype.Service;

import ac.cn.iscas.agent.repo.PointcutRepository.Pointcut;

@Service
public class TracerRepository extends AbstractRepository<TracerRepository.Tracer, PointcutRepository.Pointcut>{
  public static class Tracer {
    protected Pointcut pointcut;
    protected Tracer child;
    protected Tracer sibling;
    protected int startTime;
    
  }
 
  
}
