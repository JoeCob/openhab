package org.openhab.binding.obd.protocol;

import java.io.IOException;

import org.openhab.binding.obd.internal.OBDBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.SerialPort;
import pt.lighthouselabs.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.*;
import pt.lighthouselabs.obd.commands.engine.*;
import pt.lighthouselabs.obd.commands.*;
import pt.lighthouselabs.obd.commands.fuel.*;
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

// COMM Settings
private int commDelay = 50;

//
long commandStart;

private float AirIntakeTemp;
AirIntakeTemperatureObdCommand airIntakeTemperatureCommand = new AirIntakeTemperatureObdCommand();
private float LTFT_1;
FuelTrimObdCommand fuelTrimObdCommand = new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_1);
private float AmbientAirTemp;
AmbientAirTemperatureObdCommand ambientAirTemperatureCommand = new AmbientAirTemperatureObdCommand();
private float EngineCoolantTemp;
EngineCoolantTemperatureObdCommand engineCoolantTemperatureCommand = new EngineCoolantTemperatureObdCommand();

private int BarometricPressure;
private int FuelPressure;
private int IntakeManifoldPressure;
IntakeManifoldPressureObdCommand intakeManifoldPressureCommand = new IntakeManifoldPressureObdCommand();


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
private float fuelEconomywithMaf;
private float fuelEconomyNoMaf;
private float timingAdvance;
private String dtcCode;
private double equivRatio;

SerialPort serialPort = null;
private float oxygen1Pct;
private float oxygen1Voltage;

private float imap;
private float mafTmp;
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
		logger.debug("OBD Data Refresh Requested at {} ", refreshStart );
		
		logger.debug("Calling setEngineRpm at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setEngineRpm();
		logger.trace("Done calling setEngineRpm at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart );


		logger.debug("Calling setSleed at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setSpeed();
		logger.trace("Done calling setSpeed at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart );


		logger.debug("Calling setEngineLoad at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setEngineLoad();
		
		logger.debug("Calling setEngineCoolantTempat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setEngineCoolantTemp();
		
		
		logger.debug("Calling setIntakeManifoldPressureat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart );
		this.setIntakeManifoldPressure();
		
		logger.debug("Calling setMaf at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setMassAirflow();
		
		
		logger.debug("Calling setLTFT at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setLTFT_1();
		
		
		logger.debug("Calling setAirIntakeTemp at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart );
		this.setAirIntakeTemp();
		
		
		logger.debug("Calling setAmbientAirTemp at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setAmbientAirTemp();


		
		logger.debug("Calling setBarometricPressureat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setBarometricPressure();
		
		logger.debug("Calling setFuelPressureat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setFuelPressure();
		


		
		logger.debug("Calling setEngineRunTimeat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setEngineRuntime();
		


		
		
		logger.debug("Calling setThrottleat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setThrottle();
		logger.debug("Calling setFuelLevelat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setFuelLevel();
		logger.debug("Calling setFuelTypeat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setFuelType();
		logger.debug("Calling setFuelConsumptionat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setFuelConsumption();
		logger.debug("Calling setFuelEconomyat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setFuelEconomy();
		logger.debug("Calling setFuelEconomywithMafat {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setFuelEconomywithMaf();
		logger.debug("Calling setFuelEconomyNoMaf at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setFuelEconomyNoMaf();
		logger.debug("Calling setTimingAdvance at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setTimingAdvance();
		logger.debug("Calling setDtcCode at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setDtcCode();
		logger.debug("Calling Oxygen1 at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setOxygenSensor1();
		logger.debug("Calling setEquivRatio at {}, previous command took {} ms", System.currentTimeMillis(), System.currentTimeMillis() - commandStart  );
		this.setEquivRatio();

		logger.debug("Finished refreshing OBD Data. Total time was {}", System.currentTimeMillis() - refreshStart );
	} catch (IOException e) {
		// TODO Auto-generated catch block
		logger.debug("Serial IO failure at {}", System.currentTimeMillis() );
		throw new IOException (e);
	} catch ( NoDataException ndex ) {
		logger.debug("No Data - {} at ", ndex.toString(), System.currentTimeMillis());
		noData = true;
		//Add catch for UNABLE TO CONNECT. This would return a status from refresh and be treated at the Binging. 
		//return 1;
	} catch ( UnableToConnectException cntdex ) {
		logger.debug("Unable to Connect Exception - {} at ", cntdex.toString(), System.currentTimeMillis());
		invalidateObjects();
		//Add catch for UNABLE TO CONNECT. This would return a status from refresh and be treated at the Binging. 
		return -2;
	} catch ( Exception ex ) 
	{
		logger.debug ( "Exception while pooling from OBD - {} - at ts {}", ex.toString(), System.currentTimeMillis());
		ex.printStackTrace();
		return -1;
	}
	
	logger.debug("Refresh Done" );
	
	if (noData) { 
		return 1; } 
	else {
		return 0;
	}

}


private void invalidateObjects() {
	// TODO This method sucks. 
	// What we need to do is to find a way for the Object to return the actual failure for the object. 
	// This invalidated object should not be necessary. 
	
	logger.debug("Invalidating Object States");
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
	
	// This needs to be better. Like either a loop trough the objects, or during instantiation. 
}

public void setDelay (int delay) {
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
			logger.debug("Error getting AirtakeTemp : {}. Using Default {}.", nd.toString(), defaultAIT);
			throw nd;
	
	} catch ( Exception e ) {
		AirIntakeTemp = defaultAIT;
		logger.debug("Error getting AirtakeTemp : {}. Using Default {}.", e.toString(), defaultAIT);
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
	} catch ( Exception e ) {
		maf = INVALID;
		logger.debug("Error getting MassAirFlow : {}", e.toString());
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
		logger.debug("Error getting LTFT_1 : {}", e.toString());
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
		logger.debug("Error getting AmbientAirTemp : {}", e.toString());
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
			logger.debug("Error getting EngineCoolantTemp : {}", e.toString());
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
		logger.debug("Error getting IntakeManifoldPressure : {}", e.toString());
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
			logger.debug("Error getting EngineLoad : {}", e.toString());
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
		logger.debug("Error getting RPM : {}", e.toString());
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
		logger.debug("Error getting Speed : {}", e.toString());
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
		logger.debug("Error getting Throttle : {}", e.toString());
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
		logger.debug("Error getting FuelLevel : {}", e.toString());
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
		logger.debug("Error getting FuelType : {}", e.toString());
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
		logger.debug("Error getting FuelConsumption : {}", e.toString());
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
		logger.debug("Error setting oxygenSensor1 : {}", e.toString());
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
