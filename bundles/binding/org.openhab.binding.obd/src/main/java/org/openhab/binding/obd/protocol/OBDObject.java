package org.openhab.binding.obd.protocol;

import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;

import org.openhab.binding.obd.internal.OBDBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.SerialPort;
import pt.lighthouselabs.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.*;
import pt.lighthouselabs.obd.commands.engine.*;
import pt.lighthouselabs.obd.commands.*;
import pt.lighthouselabs.obd.commands.fuel.*;
import pt.lighthouselabs.obd.commands.control.*;
import pt.lighthouselabs.obd.exceptions.NoDataException;
import pt.lighthouselabs.obd.exceptions.UnableToConnectException;
import pt.lighthouselabs.obd.enums.FuelTrim;





public class OBDObject {

/*
		  AIR_INTAKE_TEMP("Air Intake Temperature"),
		  AMBIENT_AIR_TEMP("Ambient Air Temperature"),
		  ENGINE_COOLANT_TEMP("Engine Coolant Temperature"),
		  BAROMETRIC_PRESSURE("Barometric Pressure"),
		  FUEL_PRESSURE("Fuel Pressure"),
		  INTAKE_MANIFOLD_PRESSURE("Intake Manifold Pressure"),
		  ENGINE_LOAD("Engine Load"),
		  ENGINE_RUNTIME("Engine Runtime"),
		  ENGINE_RPM("Engine RPM"),
		  SPEED("Vehicle Speed"),
		  MAF("Mass Air Flow"),
		  THROTTLE_POS("Throttle Position"),
		  TROUBLE_CODES("Trouble Codes"),
		  FUEL_LEVEL("Fuel Level"),
		  FUEL_TYPE("Fuel Type"),
		  FUEL_CONSUMPTION("Fuel Consumption"),
		  FUEL_ECONOMY("Fuel Economy"),
		  FUEL_ECONOMY_WITH_MAF("Fuel Economy 2"),
		  FUEL_ECONOMY_WITHOUT_MAF("Fuel Economy 3"),
		  TIMING_ADVANCE("Timing Advance"),
		  DTC_NUMBER("Diagnostic Trouble Codes"),
		  EQUIV_RATIO("Command Equivalence Ratio"),
		  DISTANCE_TRAVELED_AFTER_CODES_CLEARED("Distance Traveled After Codes Cleared");
*/

//Engine Parameters
float engineVE = 85f;
float defaultAIT = 300;
float INVALID = -999;
//


//List<Integer> gearList = new ArrayList<Integer>(7);
int[] gearList;

boolean checkIgnitionObd = false;



// COMM Settings
private int commDelay = 50;

//
long commandStart;

private float AirIntakeTemp;
AirIntakeTemperatureObdCommand airIntakeTemperatureCommand = new AirIntakeTemperatureObdCommand();
private float LTFT_1;
FuelTrimObdCommand fuelTrimObdCommand = new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_1);

private float STFT_1;
FuelTrimObdCommand fuelTrimSTObdCommand = new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_1);


private float AmbientAirTemp;
AmbientAirTemperatureObdCommand ambientAirTemperatureCommand = new AmbientAirTemperatureObdCommand();
private float EngineCoolantTemp;
EngineCoolantTemperatureObdCommand engineCoolantTemperatureCommand = new EngineCoolantTemperatureObdCommand();

private int BarometricPressure;
private int FuelPressure;
private int IntakeManifoldPressure;
IntakeManifoldPressureObdCommand intakeManifoldPressureCommand = new IntakeManifoldPressureObdCommand();


private boolean ignitionStatus = false;
IgnitionObdCommand  ignitionObdCommand = new IgnitionObdCommand();


private float EngineLoad;
EngineLoadObdCommand engineLoadCommand = new EngineLoadObdCommand();
private String engineRuntime;
private int engineRpm;
EngineRPMObdCommand engineRPMCommand = new EngineRPMObdCommand();
private int speed;
SpeedObdCommand speedCommand = new SpeedObdCommand();
private float maf;
MassAirFlowObdCommand massAirFlowObdCommand = new MassAirFlowObdCommand();
private float throttle;
ThrottlePositionObdCommand throttlePositionCommand = new ThrottlePositionObdCommand();

//private troubleCodes;
private float fuelLevel;
FuelLevelObdCommand fuelLevelCommand = new FuelLevelObdCommand();
private String fuelType;
FindFuelTypeObdCommand findFuelTypeCommand = new FindFuelTypeObdCommand();
private float fuelConsumption;
FuelConsumptionRateObdCommand fuelConsumptionCommand = new FuelConsumptionRateObdCommand();
EngineOxygenSensor1ObdCommand oxygen1Command = new EngineOxygenSensor1ObdCommand();

private float fuelEconomy;
FuelEconomyObdCommand fuelEconomyCommand = new FuelEconomyObdCommand();

private int distanceTraveledSinceCodesCleared;
DistanceTraveledSinceCodesClearedObdCommand distanceTraveledSinceCodesClearedCommand  = new DistanceTraveledSinceCodesClearedObdCommand();

private double batteryVoltage;
BatteryVoltageObdCommand batteryVoltageCommand  = new BatteryVoltageObdCommand();


private int fuelStatus;
FuelStatusObdCommand fuelStatusCommand = new FuelStatusObdCommand();


private float fuelEconomywithMaf;
private float fuelEconomyNoMaf;
private float timingAdvance;
private String dtcCode;
private double equivRatio;

SerialPort serialPort = null;
private float oxygen1Pct;
private float oxygen1Voltage;

private float imap;
//private float mafTmp;
private long refreshStart;
private boolean noData = false;



private static final Logger logger = LoggerFactory.getLogger(OBDBinding.class);


public synchronized int refresh() throws IOException {
	// TODO Auto-generated method stub

	//Pools all known metrics from OBD. 
	//Each object treats its own refreshrate/timeout. Thats defined on each object for each command.
	//Invalid objects (detected by unknown or ? responses) are set as invalid at the object level and pool at the ELM327 interface wont be done. 
	// TODO: Make sure a error in one of the commands wont prevent others from running. 
	//	-- Ideas are suround each with a try/catch block or even better, 
	//  -- do a loop iterating over all OBD command objects defined and treat exceptions individually at each iteration. 

	try {
		refreshStart = System.currentTimeMillis();
		commandStart = refreshStart;
		noData = false;


		logger.debug("OBD Refresh Start" );


		// Add commands that dont depend on IGNITION before. 
		// This will allow them to be updated even if ignition is off and we get UnabletoConnect or NoData errors. 
		// This will in the future need to be broken down into seperate classes that handle this type 
		// of commands independently. 

		logger.trace("Calling setBatteryLevel at {}", System.currentTimeMillis() );
		this.setBatteryVoltage();
		logger.debug("Timing - setBattery {} ms ", System.currentTimeMillis() - commandStart );

		logger.trace("Calling setIgnitionStatus at {}", System.currentTimeMillis() );
		this.setIgnitionStatus();
		logger.debug("Timing - setIgnitionStatus {} ms ", System.currentTimeMillis() - commandStart );

		// End of ignition dependent commands. 
		// Ignition dependent commands. 

		if (this.ignitionStatus == true ) {
			logger.debug("OBD Ignition Dependent Start" );

			logger.trace("Calling setEngineRpm at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setEngineRpm();
			logger.debug("Timing - setEngineRpm {} ms ", System.currentTimeMillis() - commandStart );



			logger.trace("Calling setSpeed at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setSpeed();
			logger.debug("Timing - setSpeed {} ms ", System.currentTimeMillis() - commandStart );


			logger.trace("Calling setEngineLoad at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setEngineLoad();
			logger.debug("Timing - setEngineLoad {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setEngineCoolantTempat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setEngineCoolantTemp();
			logger.debug("Timing - setEngineCoolantTempat {} ms ", System.currentTimeMillis() - commandStart );


			logger.trace("Calling setIntakeManifoldPressureat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart );
			this.setIntakeManifoldPressure();
			logger.debug("Timing - setIntakeManifoldPressureat {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setMaf at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setMassAirflow();
			logger.debug("Timing - setMaf {} ms ", System.currentTimeMillis() - commandStart );


			logger.trace("Calling setLTFT at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setLTFT_1();
			logger.debug("Timing - setLTFT {} ms ", System.currentTimeMillis() - commandStart );


			logger.trace("Calling setAirIntakeTemp at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart );
			this.setAirIntakeTemp();
			logger.debug("Timing - setAirIntakeTemp {} ms ", System.currentTimeMillis() - commandStart );


			logger.trace("Calling setAmbientAirTemp at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setAmbientAirTemp();
			logger.debug("Timing - setAmbientAirTemp {} ms ", System.currentTimeMillis() - commandStart );



			logger.trace("Calling setBarometricPressureat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setBarometricPressure();
			logger.debug("Timing - setBarometricPressureat {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setFuelPressure at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setFuelPressure();
			logger.debug("Timing - setFuelPressure {} ms ", System.currentTimeMillis() - commandStart );		

			logger.trace("Calling setEngineRunTimeat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setEngineRuntime();
			logger.debug("Timing - setEngineRunTimeat {} ms ", System.currentTimeMillis() - commandStart );


			logger.trace("Calling setDistanceTravaledSinceCodesCleared {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setDistanceTravaledSinceCodesCleared();
			logger.debug("Timing - setDistanceTravaledSinceCodesCleared {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setThrottleat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setThrottle();
			logger.debug("Timing - setThrottleat {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setFuelLevelat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setFuelLevel();
			logger.debug("Timing - setFuelLevelat {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setFuelTypeat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setFuelType();
			logger.debug("Timing - setFuelTypeat {} ms ", System.currentTimeMillis() - commandStart );

			
			logger.trace("Calling setFuelStatus at {}", System.currentTimeMillis());
			this.setFuelStatus();
			logger.debug("Timing - setFuelStatus at {} ms ", System.currentTimeMillis() - commandStart );

			
			
			logger.trace("Calling setFuelConsumptionat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setFuelConsumption();
			logger.debug("Timing - setFuelConsumptionat {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setFuelEconomyat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setFuelEconomy();
			logger.debug("Timing - setFuelEconomyat {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setFuelEconomywithMafat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setFuelEconomywithMaf();
			logger.debug("Timing - setFuelEconomywithMafat {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setFuelEconomyNoMaf at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setFuelEconomyNoMaf();
			logger.debug("Timing - setFuelEconomyNoMaf {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setTimingAdvance at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setTimingAdvance();
			logger.debug("Timing - setTimingAdvance {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setDtcCode at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setDtcCode();
			logger.debug("Timing - setDtcCode {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling Oxygen1 at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setOxygenSensor1();
			logger.debug("Timing - Oxygen1 {} ms ", System.currentTimeMillis() - commandStart );

			logger.trace("Calling setEquivRatio at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
			this.setEquivRatio();
			logger.debug("Timing - setEquivRatio {} ms ", System.currentTimeMillis() - commandStart );
		}

		logger.debug("OBD Refresh End ({} ms).", System.currentTimeMillis() - refreshStart );

	} catch (IOException e) {
		// TODO Auto-generated catch block
		logger.error("Serial IO failure at {}", System.currentTimeMillis() );
		logger.debug("OBD Refresh Ended due to previous error.  ({} ms).", System.currentTimeMillis() - refreshStart );
		throw new IOException (e);
	} catch ( NoDataException ndex ) {
		logger.trace("No Data - {} at ", ndex.toString(), System.currentTimeMillis());
		noData = true;
		//Add catch for UNABLE TO CONNECT. This would return a status from refresh and be treated at the Binging. 
		//return 1;
	} catch ( UnableToConnectException cntdex ) {
		logger.trace("Unable to Connect Exception - {} at ", cntdex.toString(), System.currentTimeMillis());
		invalidateObjects();
		//Add catch for UNABLE TO CONNECT. This would return a status from refresh and be treated at the Binging. 
		logger.debug("OBD Refresh Ended due to previous error.  ({} ms).", System.currentTimeMillis() - refreshStart );
		return -2;
	} catch ( Exception ex ) 
	{
		logger.error ( "Exception while pooling from OBD - {} - at ts {}", ex.toString(), System.currentTimeMillis());
		ex.printStackTrace();
		logger.debug("OBD Refresh Ended due to previous error.  ({} ms).", System.currentTimeMillis() - refreshStart );
		return -1;
	}

	logger.trace("Refresh Done" );

	if (ignitionStatus) {
		if (noData) { 
			return 1; } 
		else {
			return 0;
		}
	} else {
		//this.invalidateObjects();
		// Method to reset the OBD data, since we wont be pooling. 
		
		return 3;
	}

}


private void resetValues() {
		// TODO This method sucks. 
		// What we need to do is to find a way for the Object to return the actual failure for the object. 
		// This invalidated object should not be necessary. 
		
		logger.trace("Reseting Object Valus");
		airIntakeTemperatureCommand.reset();
		fuelTrimObdCommand.reset();
		ambientAirTemperatureCommand.reset();
		engineCoolantTemperatureCommand.reset();
		intakeManifoldPressureCommand.reset();
		engineLoadCommand.reset();
		engineRPMCommand.reset();
		speedCommand.reset();
		massAirFlowObdCommand.reset();
		throttlePositionCommand.reset();
		fuelLevelCommand.reset();
		findFuelTypeCommand.reset();
		fuelConsumptionCommand.reset();
		oxygen1Command.reset();
		fuelEconomyCommand.reset();	
}
	
private void invalidateObjects() {
	// TODO This method sucks. 
	// What we need to do is to find a way for the Object to return the actual failure for the object. 
	// This invalidated object should not be necessary. 
	
	logger.trace("Invalidating Object States");
	airIntakeTemperatureCommand.isValid(false);
	fuelTrimObdCommand.isValid(false);
	ambientAirTemperatureCommand.isValid(false);
	engineCoolantTemperatureCommand.isValid(false);
	intakeManifoldPressureCommand.isValid(false);
	engineLoadCommand.isValid(false);
	engineRPMCommand.isValid(false);
	speedCommand.isValid(false);
	massAirFlowObdCommand.isValid(false);
	throttlePositionCommand.isValid(false);
	fuelLevelCommand.isValid(false);
	findFuelTypeCommand.isValid(false);
	fuelConsumptionCommand.isValid(false);
	oxygen1Command.isValid(false);
	fuelEconomyCommand.isValid(false);	
	/*try {
		this.setAirIntakeTemp();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		this.setAmbientAirTemp();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		this.setEngineCoolantTemp();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		this.setEngineLoad();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	this.setBarometricPressure();
	this.setFuelPressure();
	try {
		this.setIntakeManifoldPressure();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	this.setEngineRuntime();
	try {
		this.setEngineRpm();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		this.setSpeed();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		this.setMassAirflow();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		this.setThrottle();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		this.setFuelLevel();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		this.setFuelType();
		this.setFuelConsumption();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	this.setFuelEconomy();
	this.setFuelEconomywithMaf();
	this.setFuelEconomyNoMaf();
	this.setTimingAdvance();
	this.setDtcCode();
	try {
		this.setOxygenSensor1();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	this.setEquivRatio();
	try {
		this.setLTFT_1();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
}


public void init(SerialPort serialPort ) {
	init ( serialPort, commDelay );
	


}

public void init(SerialPort serialPort, int delay ) {
	this.serialPort = serialPort;
	this.commDelay = delay;
	
	this.setDelay(delay);
	
	// gearList array Initialization 
	gearList = new int[] {0,0,0,0,0,0,0,0};
	
	
}

public void setDelay (int delay) {
	// This needs to be better. Like either a loop trough the objects, or during instantiation. 
	airIntakeTemperatureCommand.setCommDelay(delay);
	fuelTrimObdCommand.setCommDelay(delay);
	ambientAirTemperatureCommand.setCommDelay(delay);
	engineCoolantTemperatureCommand.setCommDelay(delay);
	intakeManifoldPressureCommand.setCommDelay(delay);
	engineLoadCommand.setCommDelay(delay);
	engineRPMCommand.setCommDelay(delay);
	speedCommand.setCommDelay(delay);
	massAirFlowObdCommand.setCommDelay(delay);
	throttlePositionCommand.setCommDelay(delay);
	fuelLevelCommand.setCommDelay(delay);
	findFuelTypeCommand.setCommDelay(delay);
	fuelConsumptionCommand.setCommDelay(delay);
	oxygen1Command.setCommDelay(delay);
	fuelEconomyCommand.setCommDelay(delay);
}
/**
 * @return the airIntakeTemp
 */
public float getAirIntakeTemp() {
	/*try {
		this.setAirIntakeTemp();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
   return AirIntakeTemp;
}


/**
 * @return the ambientAirTemp
 */
public float getAmbientAirTemp() {
	/*try {
		this.setAmbientAirTemp();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return AmbientAirTemp;
}


private float getLTFT_1() {
	/*try {
		this.setLTFT_1();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return LTFT_1;
}

/**
 * @return the engineCoolantTemp
 */
public float getEngineCoolantTemp() {
	/*try {
		this.setEngineCoolantTemp();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return EngineCoolantTemp;
}

/**
 * @return the MPG from MAF on Bank 1
 */
public float getMPG() {
	
	if (this.maf != INVALID && this.speed != INVALID && this.speed > 0 ) {
		return (14.7f * 6.17f * 454f * this.speed * 0.621371f) / (3600 * this.maf);
	}
	logger.trace("MPG  set to 0 on invalid parameter {} {} {}", this.maf,this.speed, this.LTFT_1);
	return 0;
}

/**
 * @return the KML from MAF on Bank 1
 */
public float getKML() {
	return (this.getMPG()*0.425143707f);
}


/**
 * @return the MPG from Long Term TRIM on Bank 1
 */
public float getMPGLongTerm() {
	if (this.maf != INVALID && this.speed != INVALID && this.LTFT_1 != INVALID & this.speed> 0) {
		return (14.7f * (1 + this.LTFT_1/100f) * 6.17f * 454f * this.speed ) / (3600 * this.maf);
	}
	logger.trace("MPG LongTerm set to 0 on invalid parameter {} {} {}", this.maf,this.speed, this.LTFT_1  );
	return 0;
	//return (14.7f * 6.17f * 454f * this.getSpeed() * 0.621371f) / (3600 * this.getMaf());
}

/**
 * @return the KM/L from Long Term TRIM on Bank 1
 */
public float getKMLLongTerm() {
	return (this.getMPGLongTerm()*0.425143707f);
}


/**
 * @return the barometricPressure
 */
public int getBarometricPressure() {
	/*try {
		this.setBarometricPressure();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return BarometricPressure;
}


/**
 * @return the fuelPressure
 */
public int getFuelPressure() {
	/*try {
		this.setFuelPressure();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return FuelPressure;
}


/**
 * @return the intakeManifoldPressure
 */
public int getIntakeManifoldPressure() {
	/*try {
		this.setIntakeManifoldPressure();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return IntakeManifoldPressure;
}

public boolean getIgnition() {
	/*try {
		this.setIntakeManifoldPressure();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	
	if (this.engineRpm > 0) { 
		return true;
	}
	return false;
}

/**
 * @return the engineLoad
 */
public float getEngineLoad() {
	/*try {
		this.setEngineLoad();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return EngineLoad;
}


/**
 * @return the engineRuntime
 */
public String getEngineRuntime() {
	/*try {
		this.setEngineRuntime();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return engineRuntime;
}

/**
 * @return the engineRuntime
 */
public int getDistanceTravaledSinceCodesCleared() {
	/*try {
		this.setEngineRuntime();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return this.distanceTraveledSinceCodesCleared;
}

/**
 * @return the engineRuntime
 */
public double getBatteryVoltage() {
	/*try {
		this.setEngineRuntime();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return this.batteryVoltage;
}


/**
 * @return the engineRuntime
 */
public int getCurrentGear() {
	
	// Formula for RPM is (speed * gearratio * 5305 (for km/h))/tire diameter (mm). 
    //  var speed = (OBD_obdspeed.state as DecimalType).intValue
    //    var rpm  = (OBD_enginerpm.state as DecimalType).intValue
        
    int min = 8000;
	int closest = 0;
	
        if (this.speed  == 0) {
        	logger.trace( "Calculated gear is 0 since speed is 0" ) ;
    		closest =  0;        	
        } else {
        	logger.trace( "Input for gear calculatio is RPM {} and speed {} ", this.engineRpm , this.speed ); 
	        
        	//List<Integer> gearList = new ArrayList<Integer>();
        	gearList[0]=(700);
        	gearList[1]=((int) ((this.speed  * 12.9 * 5305)/621));
	        gearList[2]=((int) ((this.speed  * 7.4 * 5305)/621));
	        gearList[3]=((int) ((this.speed  * 5.1 * 5305)/621));
	        gearList[4]=((int) ((this.speed  * 4.0 * 5305)/621));
	        gearList[5]=((int) ((this.speed  * 3.2 * 5305)/621));
	        
	        
        	logger.trace("Gear list populated with {}, {}, {}, {}, {}, {}",gearList[0], gearList[1], gearList[2], gearList[3], gearList[4], gearList[5] );
        	
        	/*gearList = new int[] {700,
        			(int) ((this.speed  * 12.9 * 5305)/621),
        			(int) ((this.speed  * 7.4 * 5305)/621),
        			(int) ((this.speed  * 5.1 * 5305)/621),
        			(int) ((this.speed  * 4.0 * 5305)/621),
        			(int) ((this.speed  * 4.0 * 5305)/621),
        			(int) ((this.speed  * 3.2 * 5305)/621)};*/
        	
        	/*gearList.add(700);
        	gearList.add((int) ((this.speed  * 12.9 * 5305)/621));
	        gearList.add((int) ((this.speed  * 7.4 * 5305)/621));
	        gearList.add((int) ((this.speed  * 5.1 * 5305)/621));
	        gearList.add((int) ((this.speed  * 4.0 * 5305)/621));
	        gearList.add((int) ((this.speed  * 3.2 * 5305)/621)); */
	        //var rpm6th = (speed * 0 * 5305)/621
	 
	 		//int[] myList = Ints.asList (g0,g1,g2,g3,g4,g5);
	        //var double[] gearRatio = newArrayList( 12.9 , 7.4 , 5.1 , 4.0 , 3.2  )
	        
	        //myList[1] = 
	       
	       
	       int i = 0;
	   	   while((i=i+1) < 6)  {
	         	int diff = Math.abs((int) (gearList[i] - this.engineRpm ));
	
	        if (diff < min ) {
	            min = diff;
	            closest = i;
	        }
	   	   }	
        }
	    logger.trace ( "Calculated gear  is {} with a differente of {}", closest, min ); 
	    return closest;
      
	    //return this.distanceTraveledSinceCodesCleared;
}


/**
 * @return the engineRpm
 */
public int getEngineRpm() {
	/*try {
		this.setEngineRpm();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return engineRpm;
}


/**
 * @return the speed
 */
public int getSpeed() {
	/*try {
		this.setSpeed();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return speed;
}


/**
 * @return the maf
 */
public float getMaf() {
	/*try {
		this.setMassAirflow();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return maf; 
}


/**
 * @return the throttle
 */
public float getThrottle() {
	/*try {
		this.setThrottle();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return throttle;
}


/**
 * @return the fuelLevel
 */
public float getFuelLevel() {
	/*try {
		this.setFuelLevel();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return fuelLevel;
}


/**
 * @return the fuelType
 */
public String getFuelType() {
	/*try {
		this.setFuelType();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return fuelType;
}


/**
 * @return the fuelConsumption
 */
public float getFuelConsumption() {
	/*try {
		this.setFuelConsumption();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return fuelConsumption;
}


/**
 * @return the fuelStatus
 */
public int getFuelStatus() {
	/*try {
		this.setFuelConsumption();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return fuelStatus;
}


/**
 * @return the fuelEconomy
 */
public float getFuelEconomy() {
	/*try {
		this.setFuelEconomy();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return fuelEconomy;
}


/**
 * @return the fuelEconomywithMaf
 */
public float getFuelEconomywithMaf() {
	/*try {
		this.setFuelEconomywithMaf();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return fuelEconomywithMaf;
}


/**
 * @return the fuelEconomyNoMaf
 */
public float getFuelEconomyNoMaf() {
	/*try {
		this.setFuelEconomyNoMaf();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return fuelEconomyNoMaf;
}


/**
 * @return the timingAdvance
 */
public float getTimingAdvance() {
	/*try {
		this.setTimingAdvance();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return timingAdvance;
}


/**
 * @return the dtcCode
 */
public String getDtcCode() {
	return dtcCode;
}


/**
 * @return the equivRatio
 */
public double getEquivRatio() {
	/*try {
		this.setEquivRatio();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return equivRatio;
}


/**
 * @param airIntakeTemp the airIntakeTemp to set
 */
private void setAirIntakeTemp() throws Exception {
	
	commandStart = System.currentTimeMillis();
	try {
		
		airIntakeTemperatureCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		if (airIntakeTemperatureCommand.isValid()){
			AirIntakeTemp = airIntakeTemperatureCommand.getTemperature();
		}
	else {
		AirIntakeTemp = defaultAIT;
		}
	}
	catch ( NoDataException nd ) {
			AirIntakeTemp = defaultAIT;
			logger.trace("Error getting AirtakeTemp : {}. Using Default {}.", nd.toString(), defaultAIT);
			throw nd;
	
	} catch ( Exception e ) {
		AirIntakeTemp = defaultAIT;
		logger.trace("Error getting AirtakeTemp : {}. Using Default {}.", e.toString(), defaultAIT);
		//throw e;
	}

}

/**
 *
 */
private void setMassAirflow() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
	massAirFlowObdCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
	if ( massAirFlowObdCommand.isValid()) {
		maf = massAirFlowObdCommand.getMAF();
	} else {
		imap = (this.engineRpm * this.IntakeManifoldPressure)/this.AirIntakeTemp;
		logger.trace("IMAP {} - RPM - {} - IntakePressure {} - AirIntake {}", imap, this.engineRpm, this.IntakeManifoldPressure, this.AirIntakeTemp);
		//Calculate MAF from EngineParameters
		//IMAP = RPM * MAP / IAT
		//		MAF = (IMAP/120)*(VE/100)*(ED)*(MM)/(R)
		maf = (imap/120)*(engineVE/100)*(1.589f)*(28.97f)/(8.314f);
		logger.trace("Calculated MAF since no real sensor data available. Result was {} for imap {}", maf, imap );
	}
	if (Double.isNaN(maf)) { 
		maf = INVALID;
	}
	} catch ( Exception e ) {
		maf = INVALID;
		logger.trace("Error setting MassAirFlow : {}", e.toString());
		throw e;
	}
}



private void setLTFT_1() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
	
	fuelTrimObdCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
	if (fuelTrimObdCommand.isValid()) { 
		LTFT_1 = fuelTrimObdCommand.getValue();
		}
	else {
		LTFT_1 = INVALID;
	}
	
	} catch ( Exception e ) {
		LTFT_1 = INVALID;
		logger.trace("Error getting LTFT_1 : {}", e.toString());
		throw e;
	}
}

/**
 * @param ambientAirTemp the ambientAirTemp to set
 */
private void setAmbientAirTemp() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
	 ambientAirTemperatureCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
	 if (ambientAirTemperatureCommand.isValid()) {
		 	this.AmbientAirTemp = ambientAirTemperatureCommand.getTemperature();
	 } else {
		 this.AmbientAirTemp = INVALID;
	}
	} catch ( Exception e ) {
		this.AmbientAirTemp = INVALID;
		logger.trace("Error getting AmbientAirTemp : {}", e.toString());
		throw e;
	}
}


/**
 * @param engineCoolantTemp the engineCoolantTemp to set
 * @throws IOException 
 */
private void setEngineCoolantTemp()  throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		 engineCoolantTemperatureCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		 if ( engineCoolantTemperatureCommand.isValid() ) {
			 EngineCoolantTemp = engineCoolantTemperatureCommand.getTemperature();
		 } else {
			 EngineCoolantTemp = INVALID;
		 }
		 logger.trace("Setting engineCoolantTemp to : {}", EngineCoolantTemp );
		}  catch ( IOException  iex ){
			throw  iex;
		} catch ( Exception e ) {
			this.EngineCoolantTemp = INVALID;
			logger.trace("Error getting EngineCoolantTemp : {}", e.toString());
			throw e;
		} 
}


/**
 * @param barometricPressure the barometricPressure to set
 */
private void setBarometricPressure() {
	commandStart = System.currentTimeMillis();
	BarometricPressure = (int) INVALID;
}


/**
 * @param fuelPressure the fuelPressure to set
 */
private void setFuelPressure() {
	commandStart = System.currentTimeMillis();
	FuelPressure = (int) INVALID;
}


/**
 * @param intakeManifoldPressure the intakeManifoldPressure to set
 * @throws IOException 
 */
private void setIntakeManifoldPressure() throws  Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		intakeManifoldPressureCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		if ( intakeManifoldPressureCommand.isValid() ) {
			this.IntakeManifoldPressure = intakeManifoldPressureCommand.getMetricUnit();
		}
		else {
			this.IntakeManifoldPressure = (int) INVALID;
		}
	} catch ( IOException  iex ){
		throw new IOException (iex);
	} catch ( InterruptedException e) {
		this.IntakeManifoldPressure = (int) INVALID;
		logger.trace("Error getting IntakeManifoldPressure : {}", e.toString());
		e.printStackTrace();
		throw e;
	}
}


private void setIgnitionStatus () throws  Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		if ( checkIgnitionObd ) { 
			ignitionObdCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
			if (ignitionObdCommand.isValid()) {
				this.ignitionStatus = ignitionObdCommand.getStatus();
			}
		}
		if ( !checkIgnitionObd || !ignitionObdCommand.isValid() ) {
			if (batteryVoltageCommand.isValid()) { 
				if (this.batteryVoltage > 13) 
				{  this.ignitionStatus = true; }
				else 
				{ this.ignitionStatus = false; };				
			} else { 
				this.setEngineRpm();
				if (this.engineRpm > 0) 
					{  this.ignitionStatus = true; }
				else 
					{ this.ignitionStatus = false; };	
			}
		}
	} catch ( IOException  iex ){
		throw new IOException (iex);
	} catch ( NoDataException ndex ) {
			logger.trace("No Data getting ignition status. Setting ignition to false");
			this.ignitionStatus = false;
	} catch ( InterruptedException e) {
		this.IntakeManifoldPressure = (int) INVALID;
		logger.trace("Error getting Ignition Status : {}", e.toString());
		e.printStackTrace();
		throw e;
	}
}


/**
 * @param engineLoad the engineLoad to set
 * @throws IOException 
 */
private void setEngineLoad()  throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		 engineLoadCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		 if (engineLoadCommand.isValid()) {
			 this.EngineLoad = engineLoadCommand.getPercentage();
		 } else {
			 this.EngineLoad = INVALID;
		 }
		} catch ( IOException  iex ){
			throw new IOException (iex);
		} catch ( Exception e ) {
			this.EngineLoad = INVALID;
			logger.trace("Error getting EngineLoad : {}", e.toString());
			throw e;
			
		}
	
}


/**
 * @param engineRuntime the engineRuntime to set
 */
private void setEngineRuntime() {
	commandStart = System.currentTimeMillis();
	this.engineRuntime = "";
}

/**
 * @param DistanceTravaledSinceCodesCleared the engineRuntime to set
 */
private void setDistanceTravaledSinceCodesCleared()  throws  Exception {
	commandStart = System.currentTimeMillis();
	try {
		distanceTraveledSinceCodesClearedCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		this.distanceTraveledSinceCodesCleared = distanceTraveledSinceCodesClearedCommand.getKm();
		/*if ( distanceTraveledSinceCodesClearedCommand.isValid() ) {
			 this.distanceTraveledSinceCodesCleared = distanceTraveledSinceCodesClearedCommand.getKm();
			 this.distanceTraveledSinceCodesCleared = distanceTraveledSinceCodesClearedCommand.getKm();
		 } else {
			 this.distanceTraveledSinceCodesCleared = (int) INVALID;
		 }*/
	} catch ( IOException  iex ){
		throw new IOException (iex);
	} catch ( Exception e ) {
		this.engineRpm = 0;
		logger.trace("Error getting  distanceTraveledSinceCodesCleared: {}", e.toString());
		throw e;
	}
}


/**
 * @param DistanceTravaledSinceCodesCleared the engineRuntime to set
 */
private void setBatteryVoltage()  throws  Exception {
	commandStart = System.currentTimeMillis();
	try {
		batteryVoltageCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		 if ( batteryVoltageCommand.isValid() ) {
			 this.batteryVoltage = batteryVoltageCommand.getVoltage();
		 } else {
			 this.batteryVoltage = -1;
		 }
	} catch ( IOException  iex ){
		throw new IOException (iex);
	} catch ( Exception e ) {
		this.engineRpm = 0;
		logger.trace("Error getting  battery voltage: {}", e.toString());
		throw e;
	}
}



/**
 * @param engineRpm the engineRpm to set
 * @throws IOException 
 */
private void setEngineRpm() throws  Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		 engineRPMCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		 if ( engineRPMCommand.isValid() ) {
			 this.engineRpm = engineRPMCommand.getRPM();
		 } else {
			 this.engineRpm = (int) INVALID;
		 }
	} catch ( IOException  iex ){
		throw new IOException (iex);
	} catch ( Exception e ) {
		this.engineRpm = 0;
		logger.trace("Error getting RPM : {}", e.toString());
		throw e;
	}
}


/**
 * @param speed the speed to set
 * @throws IOException 
 */
private void setSpeed()  throws Exception  {
	commandStart = System.currentTimeMillis();
	
	try {
	speedCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
	if (speedCommand.isValid()) {
		this.speed = speedCommand.getMetricSpeed();
	} else {
		this.speed = (int) INVALID;
	}
	} catch ( IOException  iex ){
		throw new IOException (iex);
	} catch (Exception e ) {
		this.speed = 0;
		logger.trace("Error getting Speed : {}", e.toString());
		e.printStackTrace();
		throw e;
	}
}




/**
 * @param throttle the throttle to set
 */
private void setThrottle() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		throttlePositionCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		if (throttlePositionCommand.isValid()){ 
			this.throttle = throttlePositionCommand.getPercentage();
		} else {
			this.throttle = INVALID;
		}
	} catch (Exception e ) {
		this.throttle = 0;
		logger.trace("Error getting Throttle : {}", e.toString());
		throw e;
	}
}


/**
 * @param fuelLevel the fuelLevel to set fuelLevelCommand
 */
private void setFuelLevel() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		fuelLevelCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		if ( fuelLevelCommand.isValid() ) {
			this.fuelLevel = fuelLevelCommand.getFuelLevel();
		} else {
			this.fuelLevel = INVALID;
		}
	} catch (Exception e ) {
		this.fuelLevel = INVALID;
		logger.trace("Error getting FuelLevel : {}", e.toString());
		throw e;
	}
}


/**
 * @param fuelType the fuelType to set
 */
private void setFuelType() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		findFuelTypeCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		this.fuelType = findFuelTypeCommand.getFormattedResult();
	} catch (Exception e ) {
		this.fuelType = "ERROR";
		logger.trace("Error getting FuelType : {}", e.toString());
		throw e;
	}
}


/**
 * @param fuelConsumption the fuelConsumption to set
 */
private void setFuelConsumption() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		fuelConsumptionCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		if (fuelConsumptionCommand.isValid()) {
			this.fuelConsumption = fuelConsumptionCommand.getLitersPerHour();
		} else 
		{
			this.fuelConsumption = INVALID;
		}
	} catch (Exception e ) {
		this.fuelConsumption = 0;
		logger.trace("Error getting FuelConsumption : {}", e.toString());
		throw e;
	}
}


/**
 * @param fuelConsumption the fuelConsumption to set
 */
private void setFuelStatus() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		fuelStatusCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		if (fuelStatusCommand.isValid()) {
			this.fuelStatus = fuelStatusCommand.getStatus();
		} else 
		{
			this.fuelStatus = 0;
		}
	} catch (Exception e ) {
		this.fuelStatus = 0;
		logger.error("Error getting FuelConsumption : {}", e.toString());
		throw e;
	}
}


private void setOxygenSensor1() throws Exception  {
	
	commandStart = System.currentTimeMillis();
	try {
		
		oxygen1Command.run(serialPort.getInputStream(), serialPort.getOutputStream());
		if (oxygen1Command.isValid()) {
			this.oxygen1Pct = oxygen1Command.getPercentage();
			this.oxygen1Voltage = oxygen1Command.getVoltage();
			logger.trace ( "Oxygen sensor 1 returned {}, {}", this.oxygen1Pct,this.oxygen1Voltage );
		} else 
		{
			this.oxygen1Pct = INVALID;
			this.oxygen1Voltage = INVALID;
		}
	} catch (Exception e ) {
		this.oxygen1Pct = INVALID;
		this.oxygen1Voltage = INVALID;
		logger.trace("Error setting oxygenSensor1 : {}", e.toString());
		throw e;
	}
}

public float getOxygenSensor1Voltage() {
	/*try {
		this.setOxygenSensor1();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return this.oxygen1Voltage;
}

public float getOxygenSensor1Percent() {
	/*try {
		this.setOxygenSensor1();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	return this.oxygen1Pct;
}

/**
 * @param fuelEconomy the fuelEconomy to set
 */
private void setFuelEconomy() {
	commandStart = System.currentTimeMillis();
	this.fuelEconomy = INVALID;
}


/**
 * @param fuelEconomywithMaf the fuelEconomywithMaf to set
 */
private void setFuelEconomywithMaf() {
	commandStart = System.currentTimeMillis();
	this.fuelEconomywithMaf = INVALID;
}


/**
 * @param fuelEconomyNoMaf the fuelEconomyNoMaf to set
 */
private void setFuelEconomyNoMaf() {
	commandStart = System.currentTimeMillis();
	this.fuelEconomyNoMaf = INVALID;
}


/**
 * @param timingAdvance the timingAdvance to set
 */
private void setTimingAdvance() {
	commandStart = System.currentTimeMillis();
	this.timingAdvance = INVALID;
}


/**
 * @param dtcCode the dtcCode to set
 */
private void setDtcCode() {
	commandStart = System.currentTimeMillis();
	this.dtcCode = "";
}


/**
 * @param equivRatio the equivRatio to set
 */
private void setEquivRatio() {
	commandStart = System.currentTimeMillis();
	this.equivRatio = INVALID;
}



		  
}
