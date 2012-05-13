package model.portfolio;


import java.util.List;

import utils.global.Pair;

import model.database.jpa.tables.AlgorithmSettingDouble;
import model.database.jpa.tables.AlgorithmSettingLong;



/**
 * Interface to algorithms
 * 
 * @author daniel
 */
public interface IAlgorithm {
	
	/**
	 * This is the function that the scheduler will call when new stockPrices are avalible
	 * @return Should return true when done
	 */
	boolean update();
	
	/**
	 * Returns the name of this algorithm
	 * @return The name
	 */
	String getName();
	
	/**
	 * Returns a description of the algorithm
	 * @return A string representing a description of the algorithm
	 */
	String getDescription();
	
	
	// Settings-------------------------------
	List<AlgorithmSettingDouble> getDefaultDoubleSettings();
	List<AlgorithmSettingLong> getDefaultLongSettings();
	
	boolean giveDoubleSettings(List<Pair<String, Double>> doubleSettings);
	boolean giveLongSettings(List<Pair<String, Long>> longSettings);
	// ---------------------------------------
	
	void setRobot(IRobot_Algorithms robot);

	void setPortfolio(IPortfolio p);
}
