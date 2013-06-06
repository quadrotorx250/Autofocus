package imu;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;


import fr.dgac.ivy.*;
import common.TypeCalibration;
import data.Data;

/**
 * This is the class that links the calibrating program to the IVY bus in order
 * to get the RAW_DATA messages.
 * 
 * @author Florent GERVAIS
 */

public class IMU implements IvyMessageListener {
	private List <Integer> listeId;
	private Ivy bus;
	//private TypeCalibration calibration;
	private int idDrone;
	//private Data data;
	private Boolean rawOnBus = false;
	private String telemetryMode=null;
	private int reqid = 176;
	private String settings;
	private String acName;

	/**
	 * allows to get back the right RAW_DATA messages
	 * 
	 * @throws IvyException
	 */
	public IMU() {
		idDrone = -1;
		//this.data=data;
		listeId=new ArrayList<Integer>();
		System.out.println("Debut IMU");
		//this.calibration = calibration;
		// starts the bus on the default domain
		bus = new Ivy("IMU", "IMU Ready", null);
	}

	public void setId(int id){
		idDrone =id;
	}
	
	public void ListenIMU(final Data data, TypeCalibration calibration){
		try {
			// build the regexp according to parameters
			StringBuffer regexp = new StringBuffer("^");
			regexp.append(idDrone);
			regexp.append(TypeCalibration.MAGNETOMETER.equals(calibration) ? " IMU_MAG_RAW"
					: " IMU_ACCEL_RAW");

			regexp.append(" ([\\-]*[0-9]+)");
			regexp.append(" ([\\-]*[0-9]+)");
			regexp.append(" ([\\-]*[0-9]+)");
			String test = regexp.toString();
			System.out.println(test);
			bus.start(null);
			bus.bindMsg(test, new IvyMessageListener() {
				public void receive(IvyClient arg0, final String args[]) {
					// System.out.println("IMU : " + "x:" + args[0] + " y:" +
					// args[1] + " z:" + args[2]);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							data.store(Integer.valueOf(args[0]),
									Integer.valueOf(args[1]),
									Integer.valueOf(args[2]));
						}
					});
				}
			});
			
		} catch (Exception e) {
			System.out.println("Erreur d'initialisation d'IMU");
			e.printStackTrace();
		}
	}
	
	public void stopListenImu(TypeCalibration calibration){
		StringBuffer regexp = new StringBuffer("^");
		regexp.append(idDrone);
		regexp.append(TypeCalibration.MAGNETOMETER.equals(calibration) ? " IMU_MAG_RAW"
				: " IMU_ACCEL_RAW");

		regexp.append(" ([\\-]*[0-9]+)");
		regexp.append(" ([\\-]*[0-9]+)");
		regexp.append(" ([\\-]*[0-9]+)");
		String test = regexp.toString();
		bus.unBindMsg(test);
	}


	public void IvyRawListener(final int indexTelemetry) throws IvyException {
		bus.bindMsg("^" + idDrone +" [A-Za-z0-9_]+RAW(.*)",
				new IvyMessageListener() {
					public void receive(IvyClient arg0, String[] args) {
						rawOnBus = true;
					}
				});
		bus.bindMsg("^" + idDrone + " DL_VALUES ([0-9]+) (.*)",
				new IvyMessageListener() {
					public void receive(IvyClient arg0, String[] args) {
						if (Integer.valueOf(args[0]).equals(
								Integer.valueOf(idDrone)))
							telemetryMode = args[1].split(",")[indexTelemetry];
					}
				});
	}
	
	public void stopIvyRawListener(){
		bus.unBindMsg("^" + idDrone +" [A-Za-z0-9_]+RAW(.*)");
		bus.unBindMsg("^" + idDrone + " DL_VALUES ([0-9]+) (.*)");
	}

	/**
	 * Allow to test if there is any raw data on the bus
	 * 
	 */
	public Boolean isRawOnBus() {
		return rawOnBus;
	}
	public void IvyIdListener() throws IvyException {
		bus = new Ivy("IvyIdListener", "IvyIdListener Ready", null);
		bus.bindMsg("^([0-9]+) [A-Za-z0-9]", new IvyMessageListener(){
			public void receive(IvyClient arg0, String[] args) {
				if (!listeId.contains(Integer.valueOf(args[0]))) {
					listeId.add(Integer.valueOf(args[0]));
				}
			}
		});
	}
	
	/*
	public boolean idPresent(int i) throws InterruptedException {
		listeId.clear();
		Thread.sleep(1000);
		return listeId.contains(i);
	}*/
	
	public List<Integer> getList() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("probleme de thread");
			e.printStackTrace();
		}
		return listeId;
	}

	public int getTelemetryMode() {
		if (telemetryMode != null){
			return Integer.valueOf(telemetryMode);
		} else return 0;
	}
	
	public void sendMode(int id,double numbermode) throws IvyException{
		bus.sendMsg("calibrate DL_SETTING "+id+" 0 "+numbermode);
	}
	
	public void stopIdListener(){
		bus.unBindMsg("^([0-9]+) [A-Za-z0-9]");
	}

	/**Creates a client on the Ivy bus in charge to send request for config
	 * message relative to the drone given in parameter and to collect ac_name
	 * and settings
	 * @param idDrone
	 * @throws IvyException
	 * @throws InterruptedException
	 */
	public void IvyConfigListener() throws GetConfigException, IvyException, InterruptedException {
		Thread.sleep(20);
		sendRequest();
	}

	/**
	 * Bind to a config message once and then send a request message which triggers
	 * the send of the message
	 * @throws IvyException
	 * @throws InterruptedException
	 */
	public void sendRequest() throws IvyException, InterruptedException {
		// busBind only once
		bus.bindMsgOnce(("^" + reqid + " " + "[A-Za-z0-9]+ CONFIG (.*)"),
				new IvyMessageListener() {
					public void receive(IvyClient arg0, String[] args) {
						//System.out.println(args[0]);
						String temp[] = args[0].split(" ");
						settings = temp[4];
						acName = temp[6];
					}
				});
		Thread.sleep(20);
		bus.sendMsg("calibrate " + reqid + " CONFIG_REQ " + idDrone);
		Thread.sleep(1000);
		reqid++;

	}

	/** Returns the aircraft linked to the id 
	 * 
	 * @throws getConfigException
	 * @return aircraft name
	 */
	public String getAcName() throws GetConfigException {
		if (acName == null) throw new GetConfigException();
		else return acName;
	}

	/** Returns the url of the settings.xml file of the aircraft
	 * 
	 * @throws GetConfigException
	 * @return url to settings.xml
	 */
	public String getSettingsURL() throws GetConfigException {
		if (settings == null) throw new GetConfigException();
		else return settings.substring(7);
	}

	/** Updates the values of the aircraft name and the settings.xml's url
	 * 
	 * @throws GetConfigException
	 */
	public void update() throws GetConfigException {
		try {
			this.sendRequest();
		} catch (IvyException e) {
			throw new GetConfigException("Problème de lancement de bus",e);
		} catch (InterruptedException e) {
			throw new GetConfigException("Problème de lancement du thread d'écoute",e);
		}
	}
	
	/** Kill the listener on the bus once the object collected by the garbage
	 * 
	 */
	@Override
	public void finalize() {
		bus.stop();
	}
	

	@Override
	public void receive(IvyClient arg0, String[] args) {
		System.out.println(args[0] + " " + args[1] + " " + args[2]);
	}
	
}
