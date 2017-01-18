package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * Simulation setup for case study - position 2 
 * @author Luiz Bittencourt based on Harshit Gupta's examples
 * 
 */
public class Position3 {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	static List<FogDevice> mobiles = new ArrayList<FogDevice>();

	
	//Concurrent setup
	static int numOfCloudlets = 2; 
	static int numOfMobilesPerAppDCNS_Cloudlet0 = 3;
	static int numOfMobilesPerAppVRGame_Cloudlet0 = 3;
	static int numOfMobilesPerAppDCNS_Cloudlet1 = 3;
	static int numOfMobilesPerAppVRGame_Cloudlet1 = 3;
	static int numOfMobilesPerAppDCNS_Cloudlet2 = 3;
	static int numOfMobilesPerAppVRGame_Cloudlet2 = 3;
	static int placementStrategy = 0;
	static long proxyMIPS = 0;
	static long cloudletMIPS = 0;
	
	static boolean DCNSPlacementCloudOnly = true;
	static boolean VRGame0PlacementCloudOnly = false;
	static boolean VRGame1PlacementCloudOnly = false;
	
	
	//static double EEG_TRANSMISSION_TIME = 5.1;
	static double EEG_TRANSMISSION_TIME = 10;
	
	public static void main(String[] args) {
		int delay1=0, delay2=0, delay3=0, delay4=0;
		if(args.length < 10) {
			System.out.println("Usage: java -classpath ../jars/json-simple-1.1.1.jar:../jars/commons-math3-3.5/commons-math3-3.5.jar:. org.fog.test.perfeval.Position3 <cloudletMIPS> <proxyMIPS> <numOfCloudlets>  <numOfMobilesPerAppDCNS_Cloudlet0> <numOfMobilesPerAppVRGame_Cloudlet0> <numOfMobilesPerAppDCNS_Cloudlet1> <numOfMobilesPerAppVRGame_Cloudlet1> <numOfMobilesPerAppDCNS_Cloudlet2> <numOfMobilesPerAppVRGame_Cloudlet2> <strategy(1,2,3)>");
			System.exit(1);
		}
		cloudletMIPS = Long.parseLong(args[0]);
		proxyMIPS = Long.parseLong(args[1]); 
		numOfCloudlets = Integer.parseInt(args[2]); 
		numOfMobilesPerAppDCNS_Cloudlet0 = Integer.parseInt(args[3]);
		numOfMobilesPerAppVRGame_Cloudlet0 = Integer.parseInt(args[4]);
		numOfMobilesPerAppDCNS_Cloudlet1 = Integer.parseInt(args[5]);
		numOfMobilesPerAppVRGame_Cloudlet1 = Integer.parseInt(args[6]);
		numOfMobilesPerAppDCNS_Cloudlet2 = Integer.parseInt(args[7]);  //TODO -- not implemented
		numOfMobilesPerAppVRGame_Cloudlet2 = Integer.parseInt(args[8]);
		placementStrategy = Integer.parseInt(args[9]); //1 =  Cloudlet; 2 = FCFS (DCNS in the cloudlet and VRGame in the cloud if no space in the cloudlet); 3 = PriorityBased (VRGame in the cloudlet and DCNS in the cloud if no space in the cloudlet) 
		
	
		
		
		Log.printLine("Starting DCNS and VRGame concurrently with " + numOfCloudlets + "   cloudlets; DCNSCloudlet1: " + numOfMobilesPerAppDCNS_Cloudlet0 + "   \nVRGAmeCloudlet1: " + numOfMobilesPerAppVRGame_Cloudlet0  + "   \nDCNSCloudlet2: "  + numOfMobilesPerAppDCNS_Cloudlet1 + "   \nVRGAmeCloudlet2: " + numOfMobilesPerAppVRGame_Cloudlet1 + "   \nDCNSCloudlet3: "  + numOfMobilesPerAppDCNS_Cloudlet2 + "   \nVRGAmeCloudlet3: " + numOfMobilesPerAppVRGame_Cloudlet2 + "   \n and placement strategy " + placementStrategy);
	
		try {
			Log.disable();
			int num_user = 1;  //number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // trace events?
			
			CloudSim.init(num_user,  calendar,  trace_flag);
			
			String appId1 = "DCNS";
			String appId2 = "VRGame0";
			String appId3 = "VRGame1";
			String appId4 = "VRGame2";
			
			
			
			
			FogBroker broker1 = new FogBroker("brokerDCNS");	
			FogBroker broker2 = new FogBroker("brokerVRGame0");
			FogBroker broker3 = new FogBroker("brokerVRGame1");
			FogBroker broker4 = new FogBroker("brokerVRGame2");

			
			
			Application application1 = DCNS_createApplication(appId1, broker1.getId());		
			application1.setUserId(broker1.getId());
			Application application2 = VRGame0_createApplication(appId2, broker2.getId());
			application2.setUserId(broker2.getId());
			Application application3 = VRGame1_createApplication(appId3, broker3.getId());		
			application3.setUserId(broker3.getId());
			Application application4 = VRGame2_createApplication(appId4, broker4.getId());		
			application4.setUserId(broker4.getId());
			
			createFogDevices(proxyMIPS, cloudletMIPS);
			createEdgeDevicesDCNS(broker1.getId(), appId1);
			createEdgeDevicesVRGame0(broker2.getId(), appId2);
			createEdgeDevicesVRGame1(broker3.getId(), appId3);
			createEdgeDevicesVRGame2(broker4.getId(), appId4);

			
			System.out.println("==Devices==");
			for(FogDevice device : fogDevices) {
				System.out.println(device.getName() +" (" + device.getHost().getTotalMips() + ")");				
			}
			System.out.println("==Mobiles==");
			for(FogDevice device : mobiles) {
				System.out.println(device.getName() +" (" + device.getHost().getTotalMips() + ")");				
			}
			System.out.println("==Sensors==");
			for(Sensor device : sensors) {
				System.out.println(device.getName());				
			}
			System.out.println("==Actuators==");
			for(Actuator device : actuators) {
				System.out.println(device.getName());				
			}
			
					
			Controller controller = null;
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
			
			
			//DNCS
			for(FogDevice device : fogDevices){
				if(device.getName().startsWith("m-DCNS")){
					moduleMapping.addModuleToDevice("motion_detector", device.getName(), 1);  // fixing 1 instance of the Motion Detector module to each cloudlet	
				}
				if(placementStrategy == 1 || placementStrategy == 2) {
					application1.setPlacementStrategy("Mapping");
					if(device.getName().startsWith("cloudlet-0")){ // find cloudlet 			
//						System.out.println("Placing" + numOfMobilesPerAppDCNS_Cloudlet0 +" DCNS motion_detector in " + device.getName());
//						moduleMapping.addModuleToDevice("object_detector", device.getName(), numOfMobilesPerAppDCNS_Cloudlet0); // placing all instances of Object Detector module to cloudlets
//						moduleMapping.addModuleToDevice("object_tracker", device.getName(), numOfMobilesPerAppDCNS_Cloudlet0); // placing all instances of Object Tracker module to cloudlets
//						System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet0 + " DCNS object_detector in " + device.getName());
//						System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet0 + " DCNS object_tracker in " + device.getName());

					}
					if(device.getName().startsWith("cloudlet-1")){ // find cloudlet 			
						System.out.println("Placing" + numOfMobilesPerAppDCNS_Cloudlet1 +" DCNS motion_detector in " + device.getName());
						moduleMapping.addModuleToDevice("object_detector", device.getName(), numOfMobilesPerAppDCNS_Cloudlet1); // placing all instances of Object Detector module to cloudlets
						moduleMapping.addModuleToDevice("object_tracker", device.getName(), numOfMobilesPerAppDCNS_Cloudlet1); // placing all instances of Object Tracker module to cloudlets
						System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet1 + " DCNS object_detector in " + device.getName());
						System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet1 + " DCNS object_tracker in " + device.getName());
					}
				}
				if(placementStrategy == 3) {
					application1.setPlacementStrategy("Edgewards");
					if(device.getName().startsWith("cloudlet-0")){ // find cloudlet 			
//						System.out.println("Placing" + numOfMobilesPerAppDCNS_Cloudlet0 +" DCNS motion_detector in " + device.getName());
//						moduleMapping.addModuleToDevice("object_detector", device.getName(), numOfMobilesPerAppDCNS_Cloudlet0); // placing all instances of Object Detector module to cloudlets
//						moduleMapping.addModuleToDevice("object_tracker", device.getName(), numOfMobilesPerAppDCNS_Cloudlet0); // placing all instances of Object Tracker module to cloudlets
//						System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet0 + " DCNS object_detector in " + device.getName());
//						System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet0 + " DCNS object_tracker in " + device.getName());

					}
					if(device.getName().startsWith("cloudlet-1")){ // find cloudlet 			
						System.out.println("Placing" + numOfMobilesPerAppDCNS_Cloudlet1 +" DCNS motion_detector in " + device.getName());
						moduleMapping.addModuleToDevice("object_detector", device.getName(), numOfMobilesPerAppDCNS_Cloudlet1); // placing all instances of Object Detector module to cloudlets
						moduleMapping.addModuleToDevice("object_tracker", device.getName(), numOfMobilesPerAppDCNS_Cloudlet1); // placing all instances of Object Tracker module to cloudlets
						System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet1 + " DCNS object_detector in " + device.getName());
						System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet1 + " DCNS object_tracker in " + device.getName());
					}
//					System.out.println("Placing" + numOfMobilesPerAppDCNS_Cloudlet0 +" DCNS motion_detector in cloud");
//					moduleMapping.addModuleToDevice("object_detector", "cloud", numOfMobilesPerAppDCNS_Cloudlet0); // placing all instances of Object Detector module to cloudlets
//					moduleMapping.addModuleToDevice("object_tracker", "cloud", numOfMobilesPerAppDCNS_Cloudlet0); // placing all instances of Object Tracker module to cloudlets
//					System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet0 + " DCNS object_detector in cloud");
//					System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet0 + " DCNS object_tracker in cloud");
//					System.out.println("Placing" + numOfMobilesPerAppDCNS_Cloudlet1 +" DCNS motion_detector in cloud");
//					moduleMapping.addModuleToDevice("object_detector", "cloud", numOfMobilesPerAppDCNS_Cloudlet1); // placing all instances of Object Detector module to cloudlets
//					moduleMapping.addModuleToDevice("object_tracker", "cloud", numOfMobilesPerAppDCNS_Cloudlet1); // placing all instances of Object Tracker module to cloudlets
//					System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet1 + " DCNS object_detector in cloud");
//					System.out.println("Placing " + numOfMobilesPerAppDCNS_Cloudlet1 + " DCNS object_tracker in cloud");

				}

			}
			moduleMapping.addModuleToDevice("user_interface", "cloud", 1); // fixing instances of User Interface module in the Cloud
			System.out.println("Placing DCNS user_interface in" + " cloud");
			
			//VRGAME0 is always Mapping
			for(FogDevice device : fogDevices){
				application2.setPlacementStrategy("Mapping");
				if(device.getName().startsWith("cloudlet-0")){ // find cloudlet
					if(numOfMobilesPerAppVRGame_Cloudlet0 > 0) {
						moduleMapping.addModuleToDevice("connector_0", device.getName() , numOfMobilesPerAppVRGame_Cloudlet0); // fixing all instances of the Connector module to cloudlets
						System.out.println("Placing " + numOfMobilesPerAppVRGame_Cloudlet0 + " VRGame0 connector_0 in " + device.getName());
						moduleMapping.addModuleToDevice("concentration_calculator_0", device.getName(), numOfMobilesPerAppVRGame_Cloudlet0); // fixing all instances of the Concentration Calculator module to cloudlets
						System.out.println("Placing " + numOfMobilesPerAppVRGame_Cloudlet0 + " VRGame0 concentration_calculator_0 in " + device.getName());
					}
				}
				if(device.getName().startsWith("m-VRGame0")){
					moduleMapping.addModuleToDevice("client_0", device.getName(), 1);  // fixing all instances of the Client module to the Smartphones
					System.out.println("Placing VRGame client in " + device.getName());
				}
			}
			//VRGAME1 - depends on strategy
			for(FogDevice device : fogDevices){
				if(placementStrategy == 1) { // || placementStrategy == 3) {
					application3.setPlacementStrategy("Mapping");
					if(device.getName().startsWith("cloudlet-1")){ // find cloudlet 
						if(numOfMobilesPerAppVRGame_Cloudlet1 > 0) {
							moduleMapping.addModuleToDevice("connector_1", device.getName() , numOfMobilesPerAppVRGame_Cloudlet1); // fixing all instances of the Connector module to cloudlets
							System.out.println("Placing " + numOfMobilesPerAppVRGame_Cloudlet1 + " VRGame1 connector_1 in " + device.getName());
							moduleMapping.addModuleToDevice("concentration_calculator_1", device.getName(), numOfMobilesPerAppVRGame_Cloudlet1); // fixing all instances of the Concentration Calculator module to cloudlets
							System.out.println("Placing " + numOfMobilesPerAppVRGame_Cloudlet1 + " VRGame1 concentration_calculator_1 in " + device.getName());
						}
					}
					if(device.getName().startsWith("m-VRGame1")){
						moduleMapping.addModuleToDevice("client_1", device.getName(), 1);  // fixing all instances of the Client module to the Smartphones
						System.out.println("Placing VRGame client in " + device.getName());
					}
				}
				if(placementStrategy == 2  || placementStrategy == 3) { // Edgewards
					application3.setPlacementStrategy("Edgewards");
					if(device.getName().startsWith("cloudlet-1")){ // find cloudlet 
						if(numOfMobilesPerAppVRGame_Cloudlet1 > 0) {
							moduleMapping.addModuleToDevice("connector_1", device.getName() , numOfMobilesPerAppVRGame_Cloudlet1); // fixing all instances of the Connector module to cloudlets
							System.out.println("Placing " + numOfMobilesPerAppVRGame_Cloudlet1 + " VRGame1 connector_1 in " + device.getName());
							moduleMapping.addModuleToDevice("concentration_calculator_1", device.getName(), numOfMobilesPerAppVRGame_Cloudlet1); // fixing all instances of the Concentration Calculator module to cloudlets
							System.out.println("Placing " + numOfMobilesPerAppVRGame_Cloudlet1 + " VRGame1 concentration_calculator_1 in " + device.getName());
						}
					}
					if(device.getName().startsWith("m-VRGame1")){
						moduleMapping.addModuleToDevice("client_1", device.getName(), 1);  // fixing all instances of the Client module to the Smartphones
						System.out.println("Placing VRGame client in " + device.getName());
					}
				}
			}
			//VRGAME2 is always Mapping
			for(FogDevice device : fogDevices){
				application4.setPlacementStrategy("Mapping");
				if(device.getName().startsWith("cloudlet-2")){ // find cloudlet 
					if(numOfMobilesPerAppVRGame_Cloudlet2 > 0) {
						moduleMapping.addModuleToDevice("connector_2", device.getName() , numOfMobilesPerAppVRGame_Cloudlet2); // fixing all instances of the Connector module to cloudlets
						System.out.println("Placing " + numOfMobilesPerAppVRGame_Cloudlet2 + " VRGame2 connector_2 in " + device.getName());
						moduleMapping.addModuleToDevice("concentration_calculator_2", device.getName(), numOfMobilesPerAppVRGame_Cloudlet2); // fixing all instances of the Concentration Calculator module to cloudlets
						System.out.println("Placing " + numOfMobilesPerAppVRGame_Cloudlet2 + " VRGame2 concentration_calculator_2 in " + device.getName());
					}
				}
				if(device.getName().startsWith("m-VRGame2")){
					moduleMapping.addModuleToDevice("client_2", device.getName(), 1);  // fixing all instances of the Client module to the Smartphones
					System.out.println("Placing VRGame client in " + device.getName());
				}
			}

			
						
			//DNCS1
//			for(FogDevice device : fogDevices){
//				if(device.getName().startsWith("m-DCNS1")){ // names of all Smart Cameras start with 'm' 
//					moduleMapping.addModuleToDevice("motion_detector_1", device.getName(), 1);  // fixing 1 instance of the Motion Detector module to each Smart Camera
//					System.out.println("Placing DCNS motion_detector in " + device.getName());
//				}
//			}
//			moduleMapping.addModuleToDevice("user_interface_1", "cloud", 1); // fixing instances of User Interface module in the Cloud
//			System.out.println("Placing DCNS user_interface_1 in" + " cloud");
//			if(CLOUD_DCNS){
//				// if the mode of deployment is cloud-based
//				moduleMapping.addModuleToDevice("object_detector_1", "cloud", numOfCloudlets*numOfMobilesPerApp); // placing all instances of Object Detector module in the Cloud
//				moduleMapping.addModuleToDevice("object_tracker_1", "cloud", numOfCloudlets*numOfMobilesPerApp); // placing all instances of Object Tracker module in the Cloud
//				System.out.println("Placing " + numOfCloudlets*numOfMobilesPerApp + " DCNS object_detector_1 in " + "cloud");
//				System.out.println("Placing " + numOfCloudlets*numOfMobilesPerApp + " DCNS object_tracker_1 in " + "cloud");
//			}
			
			if(placementStrategy == 1) {
				delay1=0; delay2=0; delay3=0; delay4=0;
			}
			else if(placementStrategy == 2) {
				delay1=0; delay2=0; delay3=10; delay4=10;
			}
			else if(placementStrategy == 3) {
				delay1=10; delay2=0; delay3=0; delay4=0;
			}

			controller = new Controller("master-controller", fogDevices, sensors, actuators, moduleMapping);
			controller.submitApplication(application1, delay1);
			controller.submitApplication(application2, delay2);
			controller.submitApplication(application3, delay3);
			controller.submitApplication(application4, delay4);

			
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
			
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("Concurrent DCNS+VRGame finished!!");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	
	
	private static void createFogDevices(long proxyMIPS, long cloudletMIPS) {
		FogDevice cloud = createFogDevice("cloud", 44800, 40000, 10000, 10000, 0, 0.01, 16*103, 16*83.25); // creates the fog device Cloud at the apex of the hierarchy with level=0
		cloud.setParentId(-1);
		FogDevice proxy = createFogDevice("proxy-server", proxyMIPS, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333); // creates the fog device Proxy Server (level=1)
		proxy.setParentId(cloud.getId()); // setting Cloud as parent of the Proxy Server
		proxy.setUplinkLatency(100); // latency of connection from Proxy Server to the Cloud is 100 ms

		fogDevices.add(cloud);
		fogDevices.add(proxy);
		
		for(int i=0;i<numOfCloudlets;i++){
			addGw(i+"", proxy.getId(), cloudletMIPS); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
		}
		
	}
	private static FogDevice addGw(String id, int parentId, long cloudletMIPS){
		FogDevice cloudlet = createFogDevice("cloudlet-"+id, cloudletMIPS, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
		fogDevices.add(cloudlet);
		cloudlet.setParentId(parentId);
		cloudlet.setUplinkLatency(4); // latency of connection between gateways and proxy server is 4 ms
		if(id.startsWith("0")) { 
			for(int i=0;i<numOfMobilesPerAppDCNS_Cloudlet0;i++){
				FogDevice mobile = addMobile("DCNS-"+id+"-"+i, cloudlet.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
				mobile.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
				fogDevices.add(mobile);
			}
			for(int i=0;i<numOfMobilesPerAppVRGame_Cloudlet0;i++){
				FogDevice mobile1 = addMobile("VRGame0-"+id+"-"+i, cloudlet.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
				mobile1.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
				fogDevices.add(mobile1);
			}
		}
		if(id.startsWith("1")) { 
			for(int i=0;i<numOfMobilesPerAppDCNS_Cloudlet1;i++){
				FogDevice mobile = addMobile("DCNS-"+id+"-"+i, cloudlet.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
				mobile.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
				fogDevices.add(mobile);
			}
			for(int i=0;i<numOfMobilesPerAppVRGame_Cloudlet1;i++){
				FogDevice mobile2 = addMobile("VRGame1-"+id+"-"+i, cloudlet.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
				mobile2.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
				fogDevices.add(mobile2);
			}
		}
		if(id.startsWith("2")) { 
			for(int i=0;i<numOfMobilesPerAppDCNS_Cloudlet2;i++){
				FogDevice mobile = addMobile("DCNS-"+id+"-"+i, cloudlet.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
				mobile.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
				fogDevices.add(mobile);
			}
			for(int i=0;i<numOfMobilesPerAppVRGame_Cloudlet2;i++){
				FogDevice mobile2 = addMobile("VRGame2-"+id+"-"+i, cloudlet.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
				mobile2.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
				fogDevices.add(mobile2);
			}
		}
		return cloudlet;
	}
	
	private static FogDevice addMobile(String id, int parentId){
		FogDevice mobile = createFogDevice("m-"+id, 1000, 10000, 10000, 270, 3, 0, 87.53, 82.44);
		mobile.setParentId(parentId);
		mobiles.add(mobile);
		return mobile;
	}
	
	private static void createEdgeDevicesVRGame0(int userId, String appId) {
		for(FogDevice mobile : mobiles){
			String id = mobile.getName();
			if(id.startsWith("m-VRGame0")) {
				Sensor eegSensor = new Sensor("VRGame0-s--"+id+"-"+id, "EEG_0", userId, appId, new DeterministicDistribution(EEG_TRANSMISSION_TIME)); // inter-transmission time of EEG sensor follows a deterministic distribution
				sensors.add(eegSensor);
				Actuator display = new Actuator("VRGame0-a--"+id+"-"+id, userId, appId, "DISPLAY_0");
				actuators.add(display);
				eegSensor.setGatewayDeviceId(mobile.getId());
				eegSensor.setLatency(6.0);  // latency of connection between EEG sensors and the parent Smartphone is 6 ms
				display.setGatewayDeviceId(mobile.getId());
				display.setLatency(1.0);  // latency of connection between Display actuator and the parent Smartphone is 1 ms
			}	
		}
	}
	private static void createEdgeDevicesVRGame1(int userId, String appId) {
		for(FogDevice mobile : mobiles){
			String id = mobile.getName();
			if(id.startsWith("m-VRGame1")) {
				Sensor eegSensor = new Sensor("VRGame1-s--"+id+"-"+id, "EEG_1", userId, appId, new DeterministicDistribution(EEG_TRANSMISSION_TIME)); // inter-transmission time of EEG sensor follows a deterministic distribution
				sensors.add(eegSensor);
				Actuator display = new Actuator("VRGame1-a--"+id+"-"+id, userId, appId, "DISPLAY_1");
				actuators.add(display);
				eegSensor.setGatewayDeviceId(mobile.getId());
				eegSensor.setLatency(6.0);  // latency of connection between EEG sensors and the parent Smartphone is 6 ms
				display.setGatewayDeviceId(mobile.getId());
				display.setLatency(1.0);  // latency of connection between Display actuator and the parent Smartphone is 1 ms
			}	
		}
	}
	
	private static void createEdgeDevicesVRGame2(int userId, String appId) {
		for(FogDevice mobile : mobiles){
			String id = mobile.getName();
			if(id.startsWith("m-VRGame2")) {
				Sensor eegSensor = new Sensor("VRGame2-s--"+id+"-"+id, "EEG_2", userId, appId, new DeterministicDistribution(EEG_TRANSMISSION_TIME)); // inter-transmission time of EEG sensor follows a deterministic distribution
				sensors.add(eegSensor);
				Actuator display = new Actuator("VRGame2-a--"+id+"-"+id, userId, appId, "DISPLAY_2");
				actuators.add(display);
				eegSensor.setGatewayDeviceId(mobile.getId());
				eegSensor.setLatency(6.0);  // latency of connection between EEG sensors and the parent Smartphone is 6 ms
				display.setGatewayDeviceId(mobile.getId());
				display.setLatency(1.0);  // latency of connection between Display actuator and the parent Smartphone is 1 ms
			}	
		}
	}
	
	
	private static void createEdgeDevicesDCNS(int userId, String appId) {
		for(FogDevice mobile : mobiles){
			String id = mobile.getName();
			if(id.startsWith("m-DCNS")) {
				Sensor sensor = new Sensor("DCNS-s--"+id, "CAMERA", userId, appId, new DeterministicDistribution(5)); // inter-transmission time of camera (sensor) follows a deterministic distribution
				sensors.add(sensor);
				Actuator ptz = new Actuator("DCNS-ptz--"+id, userId, appId, "PTZ_CONTROL");
				actuators.add(ptz);
				sensor.setGatewayDeviceId(mobile.getId());
				sensor.setLatency(1.0);  // latency of connection between camera (sensor) and the parent Smart Camera is 1 ms
				ptz.setGatewayDeviceId(mobile.getId());
				ptz.setLatency(1.0);  // latency of connection between PTZ Control and the parent Smart Camera is 1 ms
			}	
		}
	}
	
	

	/**
	 * Creates a vanilla fog device
	 * @param nodeName name of the device to be used in simulation
	 * @param mips MIPS
	 * @param ram RAM
	 * @param upBw uplink bandwidth
	 * @param downBw downlink bandwidth
	 * @param level hierarchy level of the device
	 * @param ratePerMips cost rate per MIPS used
	 * @param busyPower
	 * @param idlePower
	 * @return
	 */
	private static FogDevice createFogDevice(String nodeName, long mips,
			int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower)
			);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fogdevice.setLevel(level);
		return fogdevice;
	}
	
		
	/**
	 * Function to create the Intelligent Surveillance application in the DDF model. 
	 * @param appId unique identifier of the application
	 * @param userId identifier of the user of the application
	 * @return
	 */
	@SuppressWarnings({"serial" })
	private static Application DCNS_createApplication(String appId, int userId){
		
		Application application = Application.createApplication(appId, userId);
		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		application.addAppModule("object_detector", 10, 550);
		application.addAppModule("motion_detector", 10, 300);
		application.addAppModule("object_tracker", 10, 300);
		application.addAppModule("user_interface", 10, 200);
		
				
		/*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */
		application.addAppEdge("CAMERA", "motion_detector", 1000, 20000, "CAMERA", Tuple.UP, AppEdge.SENSOR); // adding edge from CAMERA (sensor) to Motion Detector module carrying tuples of type CAMERA
		application.addAppEdge("motion_detector", "object_detector", 2000, 2000, "MOTION_VIDEO_STREAM", Tuple.UP, AppEdge.MODULE); // adding edge from Motion Detector to Object Detector module carrying tuples of type MOTION_VIDEO_STREAM
		application.addAppEdge("object_detector", "user_interface", 500, 2000, "DETECTED_OBJECT", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to User Interface module carrying tuples of type DETECTED_OBJECT
		application.addAppEdge("object_detector", "object_tracker", 1000, 100, "OBJECT_LOCATION", Tuple.UP, AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of type OBJECT_LOCATION
		application.addAppEdge("object_tracker", "PTZ_CONTROL", 100, 28, 100, "PTZ_PARAMS", Tuple.DOWN, AppEdge.ACTUATOR); // adding edge from Object Tracker to PTZ CONTROL (actuator) carrying tuples of type PTZ_PARAMS
		
		/*
		 * Defining the input-output relationships (represented by selectivity) of the application modules. 
		 */
		application.addTupleMapping("motion_detector", "CAMERA", "MOTION_VIDEO_STREAM", new FractionalSelectivity(1.0)); // 1.0 tuples of type MOTION_VIDEO_STREAM are emitted by Motion Detector module per incoming tuple of type CAMERA
		application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM", "OBJECT_LOCATION", new FractionalSelectivity(1.0)); // 1.0 tuples of type OBJECT_LOCATION are emitted by Object Detector module per incoming tuple of type MOTION_VIDEO_STREAM
		application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM", "DETECTED_OBJECT", new FractionalSelectivity(0.05)); // 0.05 tuples of type MOTION_VIDEO_STREAM are emitted by Object Detector module per incoming tuple of type MOTION_VIDEO_STREAM
	
		/*
		 * Defining application loops (maybe incomplete loops) to monitor the latency of. 
		 * Here, we add two loops for monitoring : Motion Detector -> Object Detector -> Object Tracker and Object Tracker -> PTZ Control
		 */
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("motion_detector");add("object_detector");add("object_tracker");}});
		final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{add("object_tracker");add("PTZ_CONTROL");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);add(loop2);}};
		
		application.setLoops(loops);
		return application;
	}
	

	/**
	 * Function to create the EEG Tractor Beam game application in the DDF model. 
	 * @param appId unique identifier of the application
	 * @param userId identifier of the user of the application
	 * @return
	 */
	@SuppressWarnings({"serial" })
	private static Application VRGame0_createApplication(String appId, int userId){
		
		Application application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)
		
		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		application.addAppModule("client_0", 10, 200); // adding module Client to the application model
		application.addAppModule("concentration_calculator_0", 10, 350); // adding module Concentration Calculator to the application model
		application.addAppModule("connector_0", 10, 100); // adding module Connector to the application model
		
		/*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */
		if(EEG_TRANSMISSION_TIME==10)
			application.addAppEdge("EEG_0", "client_0", 2000, 500, "EEG_0", Tuple.UP, AppEdge.SENSOR); // adding edge from EEG (sensor) to Client module carrying tuples of type EEG
		else
			application.addAppEdge("EEG_0", "client_0", 3000, 500, "EEG_0", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("client_0", "concentration_calculator_0", 3500, 500, "_SENSOR_0", Tuple.UP, AppEdge.MODULE); // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
		application.addAppEdge("concentration_calculator_0", "connector_0", 100, 1000, 1000, "PLAYER_GAME_STATE_0", Tuple.UP, AppEdge.MODULE); // adding periodic edge (period=1000ms) from Concentration Calculator to Connector module carrying tuples of type PLAYER_GAME_STATE
		application.addAppEdge("concentration_calculator_0", "client_0", 14, 500, "CONCENTRATION_0", Tuple.DOWN, AppEdge.MODULE);  // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
		application.addAppEdge("connector_0", "client_0", 100, 28, 1000, "GLOBAL_GAME_STATE_0", Tuple.DOWN, AppEdge.MODULE); // adding periodic edge (period=1000ms) from Connector to Client module carrying tuples of type GLOBAL_GAME_STATE
		application.addAppEdge("client_0", "DISPLAY_0", 1000, 500, "SELF_STATE_UPDATE_0", Tuple.DOWN, AppEdge.ACTUATOR);  // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE
		application.addAppEdge("client_0", "DISPLAY_0", 1000, 500, "GLOBAL_STATE_UPDATE_0", Tuple.DOWN, AppEdge.ACTUATOR);  // adding edge from Client module to Display (actuator) carrying tuples of type GLOBAL_STATE_UPDATE
		
		/*
		 * Defining the input-output relationships (represented by selectivity) of the application modules. 
		 */
		application.addTupleMapping("client_0", "EEG_0", "_SENSOR_0", new FractionalSelectivity(0.9)); // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG 
		application.addTupleMapping("client_0", "CONCENTRATION_0", "SELF_STATE_UPDATE_0", new FractionalSelectivity(1.0)); // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION 
		application.addTupleMapping("concentration_calculator_0", "_SENSOR_0", "CONCENTRATION_0", new FractionalSelectivity(1.0)); // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR 
		application.addTupleMapping("client_0", "GLOBAL_GAME_STATE_0", "GLOBAL_STATE_UPDATE_0", new FractionalSelectivity(1.0)); // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE 
	
		/*
		 * Defining application loops to monitor the latency of. 
		 * Here, we add only one loop for monitoring : EEG(sensor) -> Client -> Concentration Calculator -> Client -> DISPLAY (actuator)
		 */
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("EEG_0");add("client_0");add("concentration_calculator_0");add("client_0");add("DISPLAY_0");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};
		application.setLoops(loops);
		
		return application;
	}
		
	/**
	 * Function to create the EEG Tractor Beam game application in the DDF model. 
	 * @param appId unique identifier of the application
	 * @param userId identifier of the user of the application
	 * @return
	 */
	@SuppressWarnings({"serial" })
	private static Application VRGame1_createApplication(String appId, int userId){
		
		Application application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)
		
		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		application.addAppModule("client_1", 10, 200); // adding module Client to the application model
		application.addAppModule("concentration_calculator_1", 10, 350); // adding module Concentration Calculator to the application model
		application.addAppModule("connector_1", 10, 100); // adding module Connector to the application model
		
		/*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */
		if(EEG_TRANSMISSION_TIME==10)
			application.addAppEdge("EEG_1", "client_1", 2000, 500, "EEG_1", Tuple.UP, AppEdge.SENSOR); // adding edge from EEG (sensor) to Client module carrying tuples of type EEG
		else
			application.addAppEdge("EEG_1", "client_1", 3000, 500, "EEG_1", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("client_1", "concentration_calculator_1", 3500, 500, "_SENSOR_1", Tuple.UP, AppEdge.MODULE); // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
		application.addAppEdge("concentration_calculator_1", "connector_1", 100, 1000, 1000, "PLAYER_GAME_STATE_1", Tuple.UP, AppEdge.MODULE); // adding periodic edge (period=1000ms) from Concentration Calculator to Connector module carrying tuples of type PLAYER_GAME_STATE
		application.addAppEdge("concentration_calculator_1", "client_1", 14, 500, "CONCENTRATION_1", Tuple.DOWN, AppEdge.MODULE);  // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
		application.addAppEdge("connector_1", "client_1", 100, 28, 1000, "GLOBAL_GAME_STATE_1", Tuple.DOWN, AppEdge.MODULE); // adding periodic edge (period=1000ms) from Connector to Client module carrying tuples of type GLOBAL_GAME_STATE
		application.addAppEdge("client_1", "DISPLAY_1", 1000, 500, "SELF_STATE_UPDATE_1", Tuple.DOWN, AppEdge.ACTUATOR);  // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE
		application.addAppEdge("client_1", "DISPLAY_1", 1000, 500, "GLOBAL_STATE_UPDATE_1", Tuple.DOWN, AppEdge.ACTUATOR);  // adding edge from Client module to Display (actuator) carrying tuples of type GLOBAL_STATE_UPDATE
		
		/*
		 * Defining the input-output relationships (represented by selectivity) of the application modules. 
		 */
		application.addTupleMapping("client_1", "EEG_1", "_SENSOR_1", new FractionalSelectivity(0.9)); // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG 
		application.addTupleMapping("client_1", "CONCENTRATION_1", "SELF_STATE_UPDATE_1", new FractionalSelectivity(1.0)); // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION 
		application.addTupleMapping("concentration_calculator_1", "_SENSOR_1", "CONCENTRATION_1", new FractionalSelectivity(1.0)); // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR 
		application.addTupleMapping("client_1", "GLOBAL_GAME_STATE_1", "GLOBAL_STATE_UPDATE_1", new FractionalSelectivity(1.0)); // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE 
	
		/*
		 * Defining application loops to monitor the latency of. 
		 * Here, we add only one loop for monitoring : EEG(sensor) -> Client -> Concentration Calculator -> Client -> DISPLAY (actuator)
		 */
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("EEG_1");add("client_1");add("concentration_calculator_1");add("client_1");add("DISPLAY_1");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};
		application.setLoops(loops);
		
		return application;
	}	
	
	/**
	 * Function to create the EEG Tractor Beam game application in the DDF model. 
	 * @param appId unique identifier of the application
	 * @param userId identifier of the user of the application
	 * @return
	 */
	@SuppressWarnings({"serial" })
	private static Application VRGame2_createApplication(String appId, int userId){
		
		Application application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)
		
		/*
		 * Adding modules (vertices) to the application model (directed graph)
		 */
		application.addAppModule("client_2", 10, 200); // adding module Client to the application model
		application.addAppModule("concentration_calculator_2", 10, 350); // adding module Concentration Calculator to the application model
		application.addAppModule("connector_2", 10, 100); // adding module Connector to the application model
		
		/*
		 * Connecting the application modules (vertices) in the application model (directed graph) with edges
		 */
		if(EEG_TRANSMISSION_TIME==10)
			application.addAppEdge("EEG_2", "client_2", 2000, 500, "EEG_2", Tuple.UP, AppEdge.SENSOR); // adding edge from EEG (sensor) to Client module carrying tuples of type EEG
		else
			application.addAppEdge("EEG_2", "client_2", 3000, 500, "EEG_2", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("client_2", "concentration_calculator_2", 3500, 500, "_SENSOR_2", Tuple.UP, AppEdge.MODULE); // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
		application.addAppEdge("concentration_calculator_2", "connector_2", 100, 1000, 1000, "PLAYER_GAME_STATE_2", Tuple.UP, AppEdge.MODULE); // adding periodic edge (period=1000ms) from Concentration Calculator to Connector module carrying tuples of type PLAYER_GAME_STATE
		application.addAppEdge("concentration_calculator_2", "client_2", 14, 500, "CONCENTRATION_2", Tuple.DOWN, AppEdge.MODULE);  // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
		application.addAppEdge("connector_2", "client_2", 100, 28, 1000, "GLOBAL_GAME_STATE_2", Tuple.DOWN, AppEdge.MODULE); // adding periodic edge (period=1000ms) from Connector to Client module carrying tuples of type GLOBAL_GAME_STATE
		application.addAppEdge("client_2", "DISPLAY_2", 1000, 500, "SELF_STATE_UPDATE_2", Tuple.DOWN, AppEdge.ACTUATOR);  // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE
		application.addAppEdge("client_2", "DISPLAY_2", 1000, 500, "GLOBAL_STATE_UPDATE_2", Tuple.DOWN, AppEdge.ACTUATOR);  // adding edge from Client module to Display (actuator) carrying tuples of type GLOBAL_STATE_UPDATE
		
		/*
		 * Defining the input-output relationships (represented by selectivity) of the application modules. 
		 */
		application.addTupleMapping("client_2", "EEG_2", "_SENSOR_2", new FractionalSelectivity(0.9)); // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG 
		application.addTupleMapping("client_2", "CONCENTRATION_2", "SELF_STATE_UPDATE_2", new FractionalSelectivity(1.0)); // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION 
		application.addTupleMapping("concentration_calculator_2", "_SENSOR_2", "CONCENTRATION_2", new FractionalSelectivity(1.0)); // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR 
		application.addTupleMapping("client_2", "GLOBAL_GAME_STATE_2", "GLOBAL_STATE_UPDATE_2", new FractionalSelectivity(1.0)); // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE 
	
		/*
		 * Defining application loops to monitor the latency of. 
		 * Here, we add only one loop for monitoring : EEG(sensor) -> Client -> Concentration Calculator -> Client -> DISPLAY (actuator)
		 */
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("EEG_2");add("client_2");add("concentration_calculator_2");add("client_2");add("DISPLAY_2");}});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};
		application.setLoops(loops);
		
		return application;
	}	
	
}