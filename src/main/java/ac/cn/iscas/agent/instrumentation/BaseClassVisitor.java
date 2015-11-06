package ac.cn.iscas.agent.instrumentation;


import ac.cn.iscas.agent.filter.InterceptorFilter;
import ac.cn.iscas.agent.filter.MethodInterceptorFilter.Method;
import ac.cn.iscas.agent.repo.PointcutRepository;
import ac.cn.iscas.agent.repo.Repository;
import ac.cn.iscas.agent.repo.PointcutRepository.Pointcut;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class BaseClassVisitor extends ClassVisitor {
  private InterceptorFilter<Method> methodFilter;
  private String className;
  private Repository<Class<?>, PointcutRepository.Pointcut> pointcutRepo;

  public BaseClassVisitor(InterceptorFilter<Method> methodFilter, ClassVisitor visitor,
      String className, Repository<Class<?>, PointcutRepository.Pointcut> pointcutRepo) {
    super(Opcodes.ASM5, visitor);
    this.methodFilter = methodFilter;
    this.className = className;
    this.pointcutRepo = pointcutRepo;
  }

  /**
   * distribute the method.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature,
      String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if (pointcutRepo.findOne(Pointcut.of(className, name)).isPresent()) {
      return new BaseMethodAdcice(Opcodes.ASM5, mv, access, name, desc, pointcutRepo);
    }
    return mv;
  }
}
