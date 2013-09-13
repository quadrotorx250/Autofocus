package imu;

import java.util.ArrayList;
import java.util.List;

/**Store the date relative to an aircraft in order to 
 * facilitate reuse
 * @author Alinoe
 *
 */
public class Aircraft {
	
	/**Name of the aircraft*/
	private String name;
	/**Id of the aircraft*/
	private int id;
	/**Path to the setting file of the aircraft*/
	private String settings;
	/**The current telemetry mode of the aircraft*/
	private int mode;
	/**Says if there is raw_data linked to this airplane*/
	private boolean isRaw;
	/**List of available modes for the aircraft*/
	private List<String> modes;
	/**index telemetry for dlvalues messages*/
	private int indexTelemetry;
	
	/**create the aircraft, no default constructor
	 * 
	 * @param name
	 * @param id
	 * @param settings
	 * @param mode
	 */
	Aircraft(String name, int id, String settings, int mode, List<String> modes, int indexTelemetry) {
		this.name = new String(name);
		this.id = id;
		this.settings = new String(settings);
		this.mode = mode;
		this.modes = new ArrayList<String>(modes);
		System.out.println(modes);
		this.indexTelemetry = indexTelemetry;
	}
	
	public boolean equals(Aircraft ac) {
		return ac.getId() == this.id;
	}
	
	/**return the name of the aircraft
	 * 
	 * @return name
	 */
	String getName() {
		return name;
	}
	
	/**return a path so settings of the aicraft
	 * 
	 * @return settings path
	 */
	String getSettings() {
		return settings;
	}
	
	/**return the id of the aircraft
	 * 
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/** return the cursor on the current telemetry mode
	 * 
	 * @return mode
	 */
	public int getMode() {
		return mode;
	}
	
	/** set the value of isRaw
	 * 
	 * @return mode
	 */
	public void setRaw(boolean b) {
		isRaw = b;
	}
	
	
	/** set the current telemetry mode
	 * 
	 * @param m
	 */
	public void setMode(int m) {
		mode = m;
	}
	
	/** get is raw data
	 * 
	 */
	boolean getIsRawData() {
		return isRaw;
	}
	
	/** return the name in order to override tostring
	 *  @override
	 */
	public String toString() {
		return name;
	}
	
	/**List modes getter*/
	public List<String> getModes() {
		return modes;
	}
	
	int getIndexTelemetry() {
		return indexTelemetry;
	}
	
	/**use to copy another aicraft, more useful than just setters*/
	public void copy(Aircraft ac){
		name = new String(ac.getName());
		id = ac.getId();
		isRaw = ac.getIsRawData();
		mode = ac.getMode();
		settings = new String(ac.getSettings());
		modes = new ArrayList<String>(ac.getModes());
		indexTelemetry = ac.getIndexTelemetry();
	}
}
