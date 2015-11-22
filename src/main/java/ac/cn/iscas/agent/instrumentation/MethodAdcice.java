package ac.cn.iscas.agent.instrumentation;

import java.util.Optional;

import ac.cn.iscas.agent.filter.RootMethodFilter.AnalysisState;
import ac.cn.iscas.agent.filter.RootMethodFilter.Method;
import ac.cn.iscas.agent.repo.ClassRepository;
import ac.cn.iscas.agent.repo.ClassRepository.ClassId;
import ac.cn.iscas.agent.repo.ClassRepository.ClassMirror;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.commons.AdviceAdapter;

public class MethodAdcice extends AdviceAdapter {
  private ClassRepository classRepo;
  private ClassMirror classMirror;
  private String name;

  public MethodAdcice(MethodVisitor mv, int access, String name, String desc,
      ClassRepository classRepository, ClassMirror classMirror) {
    super(Opcodes.ASM5, mv, access, name, desc);
    this.name = name;
    this.classRepo = classRepository;
    this.classMirror = classMirror;
  }

  public void visitCode() {
    super.visitCode();
  }

  public void visitMethodInsn(final int opcode, final String owner, final String name,
      final String desc, final boolean itf) {
    //skip analysis
    if(classRepo == null) {
      super.visitMethodInsn(opcode, owner, name, desc, itf);
      return;
    }
    System.out.println("visit method insn:" + name);
    Optional<ClassMirror> callee =
        classRepo.findOne(ClassId.of(owner, classMirror.getLoader()));
    if (callee.isPresent()) {
      Method method = Method.of(name, desc, null, owner);
      if (opcode == Opcodes.INVOKESTATIC) {
        visitStaticInvoke(callee.get(), method);
      } else if (opcode == Opcodes.INVOKEVIRTUAL) {
        visitVirtualInvoke(callee.get(), method);
      } else if (opcode == Opcodes.INVOKESPECIAL) {
        visitSpecialInvoke(callee.get(), method);
      }
    }
  }

  private void visitStaticInvoke(ClassMirror mirror, Method method) {
    mirror.getMethodStates().put(method, AnalysisState.Static);
  }

  private void visitVirtualInvoke(ClassMirror mirror, Method method) {
    for (ClassMirror child : mirror.getChildren()) {
      if (child.isOverriden(method)) {
        child.getMethodStates().put(method, AnalysisState.Virtual);

      }
    }
  }

  private void visitSpecialInvoke(ClassMirror mirror, Method method) {
    mirror.getMethodStates().put(method, AnalysisState.Special);
  }

  protected void onMethodEnter() {
    System.out.println("method enter: " + name);
  }

  protected void onMethodExit(int opcode) {
    System.out.println("method exit: " + name);
  }
}
