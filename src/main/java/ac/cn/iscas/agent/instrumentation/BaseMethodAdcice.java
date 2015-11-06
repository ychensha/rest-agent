package ac.cn.iscas.agent.instrumentation;

import ac.cn.iscas.agent.repo.PointcutRepository.Pointcut;
import ac.cn.iscas.agent.repo.Repository;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.commons.AdviceAdapter;

public class BaseMethodAdcice extends AdviceAdapter {
  private Repository<Class<?>, Pointcut> pointcutRepo;

  protected BaseMethodAdcice(int api, MethodVisitor mv, int access, String name, String desc,
      Repository<Class<?>, Pointcut> pointcutRepo) {
    super(api, mv, access, name, desc);
    this.pointcutRepo = pointcutRepo;
  }

  public void visitCode() {
    super.visitCode();
  }

  public void visitMethodInsn(final int opcode, final String owner, final String name,
      final String desc, final boolean itf) {
    System.out.println("owner: " + owner);
    System.out.println("name: " + name);
    System.out.println("desc: " + desc);
    //starting update the pointcutRepo
  }

  protected void onMethodEnter() {
    System.out.println("enter");
  }

  protected void onMethodExit(int opcode) {
    System.out.println("exit");
  }
}
