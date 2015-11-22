package ac.cn.iscas.agent;

import java.lang.instrument.Instrumentation;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import ac.cn.iscas.agent.config.AgentConfiguration;
import ac.cn.iscas.agent.repo.ClassRepository;
import ac.cn.iscas.agent.repo.ClassRepository.ClassId;
import ac.cn.iscas.agent.repo.ClassRepository.ClassMirror;



public class Agent {
  public static AnnotationConfigWebApplicationContext context;
  public static Instrumentation inst;

  public static void premain(String options, Instrumentation inst) throws Exception {
    Agent.inst = inst;
    Server restServer = initServer();

    System.err.println("end of agent startup.");
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable task = new Runnable() {
      @Override
      public void run() {
        System.err.println("wake up.");
        ClassRepository repo = context.getBean(ClassRepository.class);
        repo.afterLoad();
      }
    };
    executor.schedule(task, 5, TimeUnit.SECONDS);
    restServer.start();
    inst.addTransformer(context.getBean(ClassTransformer.class), true);
  }

  private static Server initServer() throws Exception {
    Server server = new Server(9191);
    server.setHandler(getServletContextHandler(getContext()));
    return server;
  }


  public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
    premain(agentArgs, inst);
  }

  private static ServletContextHandler getServletContextHandler(WebApplicationContext context) {
    ServletContextHandler contextHandler = new ServletContextHandler();
    contextHandler.setErrorHandler(null);
    DispatcherServlet dispatcher = new DispatcherServlet();
    dispatcher.setApplicationContext(context);
    contextHandler.addServlet(new ServletHolder(dispatcher), "/*");
    contextHandler.addEventListener(new ContextLoaderListener(context));
    return contextHandler;
  }

  private static WebApplicationContext getContext() {
    context = new AnnotationConfigWebApplicationContext();
    context.setConfigLocation(AgentConfiguration.class.getCanonicalName());
    return context;
  }
}
