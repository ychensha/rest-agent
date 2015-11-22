package ac.cn.iscas.agent.repo;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Service;

import ac.cn.iscas.agent.Agent;
import ac.cn.iscas.agent.filter.RootMethodFilter.AnalysisState;
import ac.cn.iscas.agent.filter.RootMethodFilter.Method;
import ac.cn.iscas.agent.instrumentation.MethodAdcice;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

/**
 * keep track of the analysis procedure
 * 
 * @author yuanxinchen
 *
 */
@Service
public class ClassRepository
    extends AbstractRepository<ClassRepository.ClassMirror, ClassRepository.ClassId> {
  private boolean isStable() {
    for (ClassMirror mirror : map.values()) {
      if (mirror.canVisitt()) {
        System.out.println(mirror);
        return false;
      }
    }
    return true;
  }

  public void afterLoad() {
    recursiveVisitClass();
    List<ClassDefinition> defs = new ArrayList<>();
    for (ClassMirror mirror : map.values()) {
      if (mirror.canRedefine()) {
        for(Class<?> clazz : Agent.inst.getAllLoadedClasses()) {
          if(clazz.getName().equals(mirror.qualifiedName()))
            System.out.println("already load target class: " + clazz.getName());
        }
        defs.add(mirror.redefine());
      }
    }
    ClassDefinition[] classDefinitions = new ClassDefinition[defs.size()];
    int i = 0;
    for (ClassDefinition def : defs)
      classDefinitions[i++] = def;
    try {
      Agent.inst.redefineClasses(classDefinitions);
    } catch (ClassNotFoundException | UnmodifiableClassException e) {
      e.printStackTrace();
    }
  }

  private void recursiveVisitClass() {
    while (!isStable()) {
      for (ClassMirror mirror : map.values()) {
        if (mirror.canVisitt()) {
          ClassReader reader = new ClassReader(mirror.getBytecode());
          ClassWriter writer =
              new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
          reader.accept(new RecursiveClassVisitor(writer, this, mirror), ClassReader.EXPAND_FRAMES);
        }
      }
    }
  }

  @EqualsAndHashCode
  public static class ClassId {
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private String name;
    @Getter
    @Setter(lombok.AccessLevel.PROTECTED)
    private ClassLoader loader;

    public static ClassId of(String name, ClassLoader loader) {
      ClassId ret = new ClassId();
      ret.name = name;
      ret.loader = loader;
      System.out.println("create CassId for: " + name);
      return ret;
    }

    @Override
    public String toString() {
      return name + loader;
    }
  }

  @EqualsAndHashCode
  public static class ClassMirror {
    @Getter
    private byte[] bytecode;
    @Getter
    private String name;
    @Getter
    private ClassLoader loader;
    @Getter
    private Map<Method, AnalysisState> methodStates = new HashMap<>();
    @Getter
    @Setter
    private ClassMirror father;
    @Getter
    @Setter
    private List<ClassMirror> children = new ArrayList<>();

    public static ClassMirror of(byte[] bytecode, String name, ClassLoader loader) {
      ClassMirror ret = new ClassMirror();
      ret.bytecode = bytecode;
      ret.name = name;
      ret.loader = loader;
      return ret;
    }

    public ClassDefinition redefine() {
      try {
        return new ClassDefinition(Class.forName(qualifiedName(), false, loader), instrument());
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      return null;
    }
    
    public String qualifiedName() {
      return name.replace("/", ".");
    }
    
    public String binaryName() {
      return name;
    }
    

    public boolean isOverriden(Method method) {
      ClassReader reader = new ClassReader(bytecode);
      OverridenMethodClassVisitor visitor = new OverridenMethodClassVisitor();
      visitor.method = method;
      visitor.className = name;
      reader.accept(visitor, ClassReader.EXPAND_FRAMES);
      return visitor.result;
    }

    public boolean canVisitt() {
      for (Entry<Method, AnalysisState> entry : getMethodStates().entrySet()) {
        if (entry.getValue() != AnalysisState.Scanned
            && entry.getValue() != AnalysisState.Unreachable) {
          System.out.println(entry.getKey());
          return true;
        }
      }
      return false;
    }

    public boolean canRedefine() {
      for (AnalysisState state : getMethodStates().values()) {
        if (state == AnalysisState.Scanned)
          return true;
      }
      return false;
    }

    public Set<Method> visitMethods() {
      Set<Method> set = new HashSet<>();
      for (Entry<Method, AnalysisState> entry : getMethodStates().entrySet()) {
        if (entry.getValue() != AnalysisState.Unreachable
            && entry.getValue() != AnalysisState.Scanned)
          set.add(entry.getKey());
      }
      return set;
    }

    public Set<Method> instrumentMethods() {
      Set<Method> set = new HashSet<>();
      for (Entry<Method, AnalysisState> entry : getMethodStates().entrySet()) {
        if (entry.getValue() == AnalysisState.Scanned)
          set.add(entry.getKey());
      }
      return set;
    }

    public void addMethod(Method method) {
      if (isOwnMethod(method)) {
        methodStates.put(method, AnalysisState.Unreachable);
      }
    }

    private boolean isOwnMethod(Method method) {
      return name.equals(method.getOwner());
    }

    public void scanMethod(Method method) {
      // check if the target method belongs to this class
      if (isOwnMethod(method)) {
        getMethodStates().put(method, AnalysisState.Scanned);
      }
    }

    @Override
    public String toString() {
      return name + "_" + methodStates;
    }

    private class OverridenMethodClassVisitor extends ClassVisitor {
      public OverridenMethodClassVisitor() {
        super(Opcodes.ASM5);
      }

      Method method;
      boolean result;
      String className;

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature,
          String[] exceptions) {
        if (method.equals(Method.of(name, desc, signature, className)))
          result = true;
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return mv;
      }
    }


    /**
     * instrument the class according to the methodStates
     * 
     * @return
     */
    private byte[] instrument() {
      ClassReader reader = new ClassReader(bytecode);
      ClassWriter writer =
          new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
      ClassVisitor instrumenter = new InstrumentClassVisitor(writer, this);
      reader.accept(instrumenter, ClassReader.EXPAND_FRAMES);
      return writer.toByteArray();
    }
  }

  static class RecursiveClassVisitor extends ClassVisitor {
    ClassRepository classRepo;
    ClassMirror classMirror;

    public RecursiveClassVisitor(ClassVisitor writer, ClassRepository classRepository,
        ClassMirror classMirror) {
      super(Opcodes.ASM5, writer);
      this.classRepo = classRepository;
      this.classMirror = classMirror;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
        String[] exceptions) {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
      Method method = Method.of(name, desc, signature, classMirror.name);
      if (classMirror.visitMethods().contains(method)) {
        classMirror.scanMethod(method);
        return new MethodAdcice(mv, access, name, desc, classRepo, classMirror);
      }
      return mv;
    }
  }

  static class InstrumentClassVisitor extends ClassVisitor {
    ClassMirror classMirror;

    public InstrumentClassVisitor(ClassWriter writer, ClassMirror classMirror) {
      super(Opcodes.ASM5, writer);
      this.classMirror = classMirror;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
        String[] exceptions) {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
      Method method = Method.of(name, desc, signature, classMirror.name);
      if (classMirror.instrumentMethods().contains(method)) {
        classMirror.scanMethod(method);
        return new MethodAdcice(mv, access, name, desc, null, classMirror);
      }
      return mv;
    }
  }
}
