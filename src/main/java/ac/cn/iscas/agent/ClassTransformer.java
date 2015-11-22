package ac.cn.iscas.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ac.cn.iscas.agent.filter.ClassInterceptorFilter;
import ac.cn.iscas.agent.filter.RootMethodFilter;
import ac.cn.iscas.agent.instrumentation.RootClassVisitor;
import ac.cn.iscas.agent.repo.PointcutRepository.Pointcut;
import ac.cn.iscas.agent.repo.Repository;
import ac.cn.iscas.agent.repo.ClassRepository;
import ac.cn.iscas.agent.repo.ClassRepository.ClassId;
import ac.cn.iscas.agent.repo.ClassRepository.ClassMirror;
import ac.cn.iscas.agent.repo.TracerRepository.Tracer;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;

@Service
public class ClassTransformer implements ClassFileTransformer {
  @Autowired
  private ClassInterceptorFilter classFilter;
  @Autowired
  private RootMethodFilter rootMethodFilter;
  @Autowired
  private Repository<Class<?>, Pointcut> pointcutRepo;
  @Autowired
  private Repository<Tracer, Pointcut> tracerRepo;
  @Autowired
  private ClassRepository classRepo;

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer)
          throws IllegalClassFormatException {
    System.err.println(className);
    ClassMirror mirror = ClassMirror.of(classfileBuffer, className, loader);
    if (classBeingRedefined == null)
      classRepo.add(ClassId.of(className, loader), mirror);
    if (!classFilter.accept(className))
      return null;
    System.err.println(className);
    visitClass(mirror);
    return null;
  }

  // visit class
  // process: filter(class) -> visitClass
  private byte[] visitClass(ClassMirror classMirror) {
    ClassReader reader = new ClassReader(classMirror.getBytecode());
    ClassWriter writer =
        new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    RootClassVisitor classVisitor =
        new RootClassVisitor(writer, rootMethodFilter, classRepo, classMirror);
    reader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
    return writer.toByteArray();
  }



  private String normalize(String className) {
    return className.replace('/', '.');
  }
}
