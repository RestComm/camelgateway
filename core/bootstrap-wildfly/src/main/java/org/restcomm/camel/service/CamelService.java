package org.restcomm.camel.service;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.as.controller.services.path.PathManager;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.mobicents.protocols.ss7.scheduler.DefaultClock;
import org.mobicents.protocols.ss7.scheduler.Scheduler;
import org.mobicents.ss7.management.console.ShellExecutor;
import org.mobicents.ss7.management.console.ShellServer;
import org.mobicents.ss7.management.console.ShellServerWildFly;
import org.mobicents.ss7.service.SS7ServiceInterface;
import org.restcomm.camelgateway.CamelManagement;
import org.restcomm.camelgateway.CamelShellExecutor;

import javolution.util.FastList;

public class CamelService implements Service<CamelService> {

	public static final CamelService INSTANCE = new CamelService();

	private final Logger log = Logger.getLogger(CamelService.class);

	public static ServiceName getServiceName() {
		return ServiceName.of("restcomm", "camel-service");
	}

	private final InjectedValue<SS7ServiceInterface> ss7Service = new InjectedValue<SS7ServiceInterface>();

	public InjectedValue<SS7ServiceInterface> getSS7Service() {
		return ss7Service;
	}

	private final InjectedValue<PathManager> pathManagerInjector = new InjectedValue<PathManager>();

	public InjectedValue<PathManager> getPathManagerInjector() {
		return pathManagerInjector;
	}

	private final InjectedValue<MBeanServer> mbeanServer = new InjectedValue<MBeanServer>();

	public InjectedValue<MBeanServer> getMbeanServer() {
		return mbeanServer;
	}

	private static final String DATA_DIR = "jboss.server.data.dir";

	private ModelNode fullModel;

	private Scheduler schedulerMBean = null;
	private CamelManagement camelManagementMBean = null;
	private CamelShellExecutor camelShellExecutor = null;

	private ShellServer shellExecutorMBean = null;

	public void setModel(ModelNode model) {
		this.fullModel = model;
	}

	private ModelNode peek(ModelNode node, String... args) {
		for (String arg : args) {
			if (!node.hasDefined(arg)) {
				return null;
			}
			node = node.get(arg);
		}
		return node;
	}

	private String getPropertyString(String mbeanName, String propertyName, String defaultValue) {
		String result = defaultValue;
		ModelNode propertyNode = peek(fullModel, "mbean", mbeanName, "property", propertyName);
		if (propertyNode != null && propertyNode.isDefined()) {
			// log.debug("propertyNode: "+propertyNode);
			// todo: test TYPE?
			result = propertyNode.get("value").asString();
		}
		return (result == null) ? defaultValue : result;
	}

	private int getPropertyInt(String mbeanName, String propertyName, int defaultValue) {
		int result = defaultValue;
		ModelNode propertyNode = peek(fullModel, "mbean", mbeanName, "property", propertyName);
		if (propertyNode != null && propertyNode.isDefined()) {
			// log.debug("propertyNode: "+propertyNode);
			// todo: test TYPE?
			result = propertyNode.get("value").asInt();
		}
		return result;
	}

	@Override
	public CamelService getValue() throws IllegalStateException, IllegalArgumentException {
		return this;
	}

	@Override
	public void start(StartContext context) throws StartException {

		log.info("Starting CamelService");

		this.camelManagementMBean = initManagementMBean();

		if (shellExecutorExists()) {

			this.schedulerMBean = initSchedulerMBean();
			this.camelShellExecutor = initShellExecutor();

			shellExecutorMBean = null;
			try {
				FastList<ShellExecutor> shellExecutors = new FastList<ShellExecutor>();
				shellExecutors.add(camelShellExecutor);
				shellExecutors.add(ss7Service.getValue().getBeanTcapExecutor());
				shellExecutors.add(ss7Service.getValue().getBeanM3uaShellExecutor());
				shellExecutors.add(ss7Service.getValue().getBeanSctpShellExecutor());
				shellExecutors.add(ss7Service.getValue().getBeanSccpExecutor());

				String address = getPropertyString("ShellExecutor", "address", "127.0.0.1");
				int port = getPropertyInt("ShellExecutor", "port", 3435);
				String securityDomain = getPropertyString("ShellExecutor", "securityDomain", "jmx-console");

				shellExecutorMBean = new ShellServerWildFly(schedulerMBean, shellExecutors);
				shellExecutorMBean.setAddress(address);
				shellExecutorMBean.setPort(port);
				shellExecutorMBean.setSecurityDomain(securityDomain);
			} catch (Exception e) {
				throw new StartException("ShellExecutor MBean creating is failed: " + e.getMessage(), e);
			}

			// starting
			try {
				schedulerMBean.start();
				shellExecutorMBean.start();
			} catch (Exception e) {
				throw new StartException("MBeans starting is failed: " + e.getMessage(), e);
			}
		}
	}

	private Scheduler initSchedulerMBean() {
		Scheduler schedulerMBean = null;
		try {
			schedulerMBean = new Scheduler();
			DefaultClock ss7Clock = initSs7Clock();
			schedulerMBean.setClock(ss7Clock);
		} catch (Exception e) {
			log.warn("SS7Scheduler MBean creating is failed: " + e);
		}
		return schedulerMBean;
	}

	private DefaultClock initSs7Clock() {
		DefaultClock ss7Clock = null;
		try {
			ss7Clock = new DefaultClock();
		} catch (Exception e) {
			log.warn("SS7Clock MBean creating is failed: " + e);
		}
		return ss7Clock;
	}

	private CamelManagement initManagementMBean() throws StartException {
		String dataDir = pathManagerInjector.getValue().getPathEntry(DATA_DIR).resolvePath();
		CamelManagement camelManagementMBean = CamelManagement.getInstance("CamelManagement");
		camelManagementMBean.setPersistDir(dataDir);

		try {
			camelManagementMBean.start();
		} catch (Exception e) {
			throw new StartException("camelManagementMBean MBean creating is failed: " + e.getMessage(), e);
		}
		registerMBean(camelManagementMBean, "org.restcomm.camel.domain:name=CamelManagement");
		return camelManagementMBean;
	}

	private CamelShellExecutor initShellExecutor() throws StartException {
		try {
			CamelShellExecutor camelShellExecutor = new CamelShellExecutor();
			camelShellExecutor.setCamelManagement(camelManagementMBean);
			return camelShellExecutor;
		} catch (Exception e) {
			throw new StartException("CamelShellExecutor MBean creating is failed: " + e.getMessage(), e);
		}
	}

	private boolean shellExecutorExists() {
		ModelNode shellExecutorNode = peek(fullModel, "mbean", "ShellExecutor");
		return shellExecutorNode != null;
	}

	@Override
	public void stop(StopContext context) {
		log.info("Stopping CamelExtension Service");

		// scheduler - stop
		try {
			if (shellExecutorMBean != null)
				shellExecutorMBean.stop();
			if (schedulerMBean != null)
				schedulerMBean.stop();
		} catch (Exception e) {
			log.warn("MBean stopping is failed: " + e);
		}
	}

	private void registerMBean(Object mBean, String name) throws StartException {
		try {
			getMbeanServer().getValue().registerMBean(mBean, new ObjectName(name));
		} catch (Throwable e) {
			throw new StartException(e);
		}
	}

	@SuppressWarnings("unused")
	private void unregisterMBean(String name) {
		try {
			getMbeanServer().getValue().unregisterMBean(new ObjectName(name));
		} catch (Throwable e) {
			log.error("failed to unregister mbean", e);
		}
	}
}
