package ac.cn.iscas.agent.instrumentation;



import ac.cn.iscas.agent.filter.RootMethodFilter;
import ac.cn.iscas.agent.filter.RootMethodFilter.Method;
import ac.cn.iscas.agent.repo.ClassRepository;
import ac.cn.iscas.agent.repo.ClassRepository.ClassId;
import ac.cn.iscas.agent.repo.ClassRepository.ClassMirror;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;


/**
 * created according to the root method pattern.</br>
 * build the father-child relation between classes.</br>
 * only visit root method.</br>
 * 
 * @author yuanxinchen
 *
 */
public class RootClassVisitor extends ClassVisitor {
  private RootMethodFilter rootFilter;
  private ClassRepository classRepo;
  private ClassMirror classMirror;

  public RootClassVisitor(ClassVisitor visitor, RootMethodFilter methodFilter,
      ClassRepository classRepo, ClassMirror classMirror) {
    super(Opcodes.ASM5, visitor);
    this.rootFilter = methodFilter;
    this.classRepo = classRepo;
    this.classMirror = classMirror;
  }

  /**
   * record superclass
   */
  @Override
  public void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    ClassId fatherId = ClassId.of(superName, classMirror.getLoader());
    if (!"Object".equals(superName) && classRepo.findOne(fatherId).isPresent()) {
      ClassMirror father = classRepo.findOne(ClassId.of(superName, classMirror.getLoader())).get();
      ClassMirror child = classRepo.findOne(ClassId.of(name, classMirror.getLoader())).get();
      child.setFather(classRepo.findOne(ClassId.of(superName, classMirror.getLoader())).get());
      father.getChildren().add(child);
    }
  }

  /**
   * distribute the method.
   */
  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature,
      String[] exceptions) {
    // check if is the root method or reachable method
    Method method = Method.of(name, desc, signature, classMirror.getName());
    System.out.println(method);
    classMirror.addMethod(method);
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if (rootFilter.accept(method)) {
      classMirror.scanMethod(method);
      return new MethodAdcice(mv, access, name, desc, classRepo, classMirror);
    }
    return mv;
  }
}
