package ac.cn.iscas.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ac.cn.iscas.agent.filter.InterceptorFilter;
import ac.cn.iscas.agent.filter.MethodInterceptorFilter;
import ac.cn.iscas.agent.instrumentation.BaseClassVisitor;
import ac.cn.iscas.agent.repo.PointcutRepository;
import ac.cn.iscas.agent.repo.PointcutRepository.Pointcut;
import ac.cn.iscas.agent.repo.Repository;
import ac.cn.iscas.agent.repo.TracerRepository.Tracer;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;

@Service
public class ClassTransformer implements ClassFileTransformer {
  @Autowired
  private InterceptorFilter<String> classFilter;
  @Autowired
  private InterceptorFilter<MethodInterceptorFilter.Method> methodFilter;
  @Autowired
  private Repository<Class<?>, Pointcut> pointcutRepo;
  @Autowired
  private Repository<Tracer, Pointcut> tracerRepo;

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer)
          throws IllegalClassFormatException {
    if (!classFilter.accept(className))
      return null;
    // classBeingREdefined != null
    // 1. to be transformed
    // 2. to be reset
    return visitClass(loader, className, classfileBuffer);
  }

  // visit class
  // process: filter(class) -> visitClass
  private byte[] visitClass(ClassLoader loader, String className, byte[] original) {
    ClassReader reader = new ClassReader(original);
    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    reader.accept(new BaseClassVisitor(methodFilter, writer, className, pointcutRepo), ClassReader.EXPAND_FRAMES);
    return writer.toByteArray();
  }



  private String normalize(String className) {
    return className.replace('/', '.');
  }
}
