/**
 * Package of the IMU. This is all the interactions with the IVY BUS
 */
package imu;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import calibrate.PrintLog;

import common.TypeCalibration;

import data.Data;
import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;

/**
 * This is the class that links the calibrating program to the IVY bus in order
 * to:
 * - get the RAW_DATA messages on the ivy bus.
 * - get the drone ID and telemetry mode.
 * - set the telemetry mode of the drone.
 * 
 * 
 * @author Florent GERVAIS
 */

public class IMU implements IvyMessageListener {
	/**
	 * this is the list of drone's id currently sending on the IVY BUS
	 */
	private List<Integer> listeId;
	/**
	 * the bus used by the drone
	 */
	private Ivy bus;
	/**
	 * the idDrone currently used for the calibration
	 */
	private int idDrone;
	/**
	 * the type of the current calibration. null because initialized after the
	 * class IMU
	 */
	private TypeCalibration calibration = null;
	/**
	 * the data generated by the IMU. null because initialized after that the
	 * class is initialized with the use of
	 */
	private Data data = null;
	/**
	 * return true if the RAW DATA MODE is detected
	 */
	private Boolean rawOnBus = false;
	/**
	 * telemetryMode is the current telemetry mode of the drone
	 */
	private String telemetryMode = null;
	/**
	 * id of the config request
	 */
	private int reqid = 42;
	/**
	 * the setting mode
	 */
	private String settings;
	/**
	 * aircraft name
	 */
	private String acName;
	/**
	 * log file
	 */
	private PrintLog log;
	/**
	 * label that checks the connection of the drone during the calibration
	 */
	private JLabel label;
	
	/**
	 * allows to get back the right RAW_DATA messages
	 * 
	 * @throws IvyException
	 */
	public IMU(final JLabel label) throws IvyException {
		this.label=label;
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				label.setBackground(Color.red);
			}
		});
		this.log = null;
		idDrone = -1;
		// this.data=data;
		listeId = new ArrayList<Integer>();
		System.out.println("Debut IMU");
		// this.calibration = calibration;
		// starts the bus on the default domain
		bus = new Ivy("IMU", "IMU Ready", null);
		bus.start(null);
	}

	/**
	 * store the id of the drone that will be used to get the right data on the
	 * bus
	 * 
	 * @param id
	 *            drone id
	 */
	public void setId(int id) {
		idDrone = id;
	}

	/**
	 * method called to listen the RAW DATA messages on the IVY bus
	 * 
	 * @param data
	 * @param calibration
	 */
	public void ListenIMU(final Data data, final TypeCalibration calibration,final PrintLog log) {
		try {

			this.log=log;
			this.data = data;
			this.calibration = calibration;
			final Timer timer = new Timer(1000,new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					SwingUtilities.invokeLater( new Runnable() {
						public void run() {
							label.setBackground(Color.red);
						}
					});
				}
			});
			
			// build the regexp according to parameters
			StringBuffer regexp = new StringBuffer("^");
			regexp.append(idDrone);
			regexp.append(TypeCalibration.MAGNETOMETER.equals(calibration) ? " IMU_MAG_RAW"
					: " IMU_ACCEL_RAW");

			regexp.append(" ([\\-]*[0-9]+)");
			regexp.append(" ([\\-]*[0-9]+)");
			regexp.append(" ([\\-]*[0-9]+)");
			String test = regexp.toString();
			bus.bindMsg(test, new IvyMessageListener() {
				public void receive(IvyClient arg0, final String args[]) {
							data.store(Integer.valueOf(args[0]),
									Integer.valueOf(args[1]),
									Integer.valueOf(args[2]));
							log.add(idDrone
									+ (TypeCalibration.MAGNETOMETER
											.equals(calibration) ? " IMU_MAG_RAW"
											: " IMU_ACCEL_RAW") + " " + args[0]
									+ " " + args[1] + " " + args[2]);
							timer.restart();
							SwingUtilities.invokeLater( new Runnable() {
								public void run() {
									label.setBackground(Color.green);
								}
							});
				}
			});

		} catch (Exception e) {
			System.out.println("Erreur d'initialisation d'IMU");
			e.printStackTrace();
		}
	}

	/**
	 * getter of the type of calibration
	 * 
	 * @return
	 */
	public TypeCalibration getCalibration() {
		return calibration;
	}

	/**
	 * release the handle on data
	 */
	public void deleteDataLog() {
		data = null;
		log = null;
	}

	/**
	 * unbind the imu from data messages
	 * 
	 * @param calibration
	 *            type of the current calibration
	 */
	public void stopListenImu(TypeCalibration calibration) {
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

	/**
	 * read the bus to find raw messages and telemetry mode
	 * 
	 * @param indexTelemetry
	 *            the position of the telemetrymode in the settings.xml of the
	 *            drone
	 * @throws IvyException
	 */
	public void IvyRawListener(final int indexTelemetry) throws IvyException {
		bus.bindMsg("^" + idDrone + " [A-Za-z0-9_]+RAW(.*)",
				new IvyMessageListener() {
					public void receive(IvyClient arg0, String[] args) {
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						rawOnBus = true;
					}
				});
		bus.bindMsg("^ground" + " DL_VALUES ([0-9]+) (.*)",
				new IvyMessageListener() {
					public void receive(IvyClient arg0, String[] args) {
						if (Integer.valueOf(args[0]).equals(
								Integer.valueOf(idDrone)))
							// System.out.println("indexTelemetry"+
							// indexTelemetry);
							telemetryMode = args[1].split(",")[indexTelemetry - 1];
						// it is considered that the two first DL_SETTINGS of
						// the .XML of
						// the drone are unused
					}
				});
	}

	/**
	 * stop the reading of the rawlistener on the bus
	 */
	public void stopIvyRawListener() {
		bus.unBindMsg("^" + idDrone + " [A-Za-z0-9_]+RAW(.*)");
		bus.unBindMsg("^" + idDrone + " DL_VALUES ([0-9]+) (.*)");
	}

	/**
	 * return true if there is any raw data on the bus
	 */
	public Boolean isRawOnBus() {
		return rawOnBus;
	}

	/**
	 * read the bus to find the different id on the bus
	 * 
	 * @throws IvyException
	 */
	public void IvyIdListener() throws IvyException {
		listeId.clear();
		bus.bindMsg("^([0-9]+) [A-Za-z0-9]", new IvyMessageListener() {
			public void receive(IvyClient arg0, String[] args) {
				if (!listeId.contains(Integer.valueOf(args[0]))) {
					listeId.add(Integer.valueOf(args[0]));
				}
			}
		});
	}

	/*
	 * public boolean idPresent(int i) throws InterruptedException {
	 * listeId.clear(); Thread.sleep(1000); return listeId.contains(i); }
	 */
	/**
	 * return the list of drone's id present on the ivy bus
	 * 
	 * @return the list of id currently connected to the ivy bus
	 */
	public List<Integer> getList() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("probleme de thread");
			e.printStackTrace();
		}
		return listeId;
	}
	/**
	 * thanks to that method it is possible to reset the listeId
	 */
	public void resetListeId() {
		listeId.clear();
	}
	
	/**
	 * return the number of the telemetry mode
	 * 
	 * @return the telemetry mode
	 */
	public Integer getTelemetryMode() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (telemetryMode != null) {
			return Double.valueOf(telemetryMode).intValue();
		} else
			return 0;
	}

	/**
	 * send the new mode to change the kind of datas sent on the IVY bus by the
	 * drone
	 * 
	 * @param id
	 *            drone's id
	 * @param numbermode
	 *            number of the mode you want to commit to the IMU
	 * @throws IvyException
	 */
	public void sendMode(int id, double numbermode) throws IvyException {
		bus.sendMsg("calibrate DL_SETTING " + id + " 0 " + numbermode);
	}

	/**
	 * stop the id listener
	 */

	public void stopIdListener() {
		bus.unBindMsg("^([0-9]+) [A-Za-z0-9]");
	}

	/**
	 * Creates a client on the Ivy bus in charge to send request for config
	 * message relative to the drone given in parameter and to collect ac_name
	 * and settings
	 * 
	 * @param idDrone
	 * @throws IvyException
	 * @throws InterruptedException
	 */
	public void IvyConfigListener() throws GetConfigException, IvyException,
			InterruptedException {
		Thread.sleep(20);
		sendRequest();
	}

	/**
	 * Bind to a config message once and then send a request message which
	 * triggers the send of the message
	 * 
	 * @throws IvyException
	 * @throws InterruptedException
	 */
	public void sendRequest() throws IvyException, InterruptedException {
		// busBind only once
		bus.bindMsgOnce(("^" + reqid + " " + "[A-Za-z0-9]+ CONFIG (.*)"),
				new IvyMessageListener() {
					public void receive(IvyClient arg0, String[] args) {
						// System.out.println(args[0]);
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

	/**
	 * Returns the aircraft name linked to the id
	 * 
	 * @throws getConfigException
	 * @return aircraft name
	 */
	public String getAcName() throws GetConfigException {
		if (acName == null)
			throw new GetConfigException();
		else
			return acName;
	}

	/**
	 * Returns the url of the settings.xml file of the aircraft
	 * 
	 * @throws GetConfigException
	 * @return url to settings.xml
	 */
	public String getSettingsURL() throws GetConfigException {
		if (settings == null)
			throw new GetConfigException();
		else
			return settings.substring(7);
	}

	/**
	 * Updates the values of the aircraft name and the settings.xml's url
	 * 
	 * @throws GetConfigException
	 */
	public void update() throws GetConfigException {
		try {
			this.sendRequest();
		} catch (IvyException e) {
			throw new GetConfigException("Problème de lancement de bus", e);
		} catch (InterruptedException e) {
			throw new GetConfigException(
					"Problème de lancement du thread d'écoute", e);
		}
	}

	/**
	 * Kill the listener on the bus once the object collected by the garbage
	 */
	@Override
	public void finalize() {
		bus.stop();
	}

	@Override
	public void receive(IvyClient arg0, String[] args) {
		System.out.println(args[0] + " " + args[1] + " " + args[2]);
	}

	/**
	 * getter of data
	 * 
	 * @return data
	 */
	public Data getData() {
		return data;
	}

	/**
	 * getter of the log
	 * 
	 * @return log
	 */
	public PrintLog getLog() {
		return log;

	}
}
