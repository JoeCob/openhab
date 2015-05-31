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
	
	
	
private float AirIntakeTemp;
AirIntakeTemperatureObdCommand airIntakeTemperatureCommand = new AirIntakeTemperatureObdCommand();
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
private float throttle;
ThrottlePositionObdCommand throttlePositionCommand = new ThrottlePositionObdCommand();

//private troubleCodes;
private float fuelLevel;
FuelLevelObdCommand fuelLevelCommand = new FuelLevelObdCommand();
private String fuelType;
FindFuelTypeObdCommand findFuelTypeCommand = new FindFuelTypeObdCommand();
private float fuelConsumption;
FuelConsumptionRateObdCommand fuelConsumptionCommand = new FuelConsumptionRateObdCommand();
private float fuelEconomy;
FuelEconomyObdCommand fuelEconomyCommand = new FuelEconomyObdCommand();
private float fuelEconomywithMaf;
private float fuelEconomyNoMaf;
private float timingAdvance;
private String dtcCode;
private double equivRatio;

SerialPort serialPort = null;

private static final Logger logger = LoggerFactory.getLogger(OBDBinding.class);


public synchronized void refresh() {
	// TODO Auto-generated method stub
	
	logger.debug("Initiating Refresh" );
	
	logger.debug("Calling setAirIntakeTemp" );
	this.setAirIntakeTemp();
	logger.debug("Calling setAmbientAirTemp" );
	this.setAmbientAirTemp();
	logger.debug("Calling setEngineCoolantTemp" );
	this.setEngineCoolantTemp();
	logger.debug("Calling setEngineLoad" );
	this.setEngineLoad();
	logger.debug("Calling setBarometricPressure" );
	this.setBarometricPressure();
	logger.debug("Calling setFuelPressure" );
	this.setFuelPressure();
	logger.debug("Calling setIntakeManifoldPressure" );
	this.setIntakeManifoldPressure();
	logger.debug("Calling setEngineRunTime" );
	this.setEngineRuntime();
	logger.debug("Calling setEngineRpm" );
	this.setEngineRpm();
	logger.debug("Calling setSpeed" );
	this.setSpeed();
	logger.debug("Calling setMaf" );
	this.setMaf();
	logger.debug("Calling setThrottle" );
	this.setThrottle();
	logger.debug("Calling setFuelLevel" );
	this.setFuelLevel();
	logger.debug("Calling setFuelType" );
	this.setFuelType();
	logger.debug("Calling setFuelConsumption" );
	this.setFuelConsumption();
	logger.debug("Calling setFuelEconomy" );
	this.setFuelEconomy();
	logger.debug("Calling setFuelEconomywithMaf" );
	this.setFuelEconomywithMaf();
	logger.debug("Calling setFuelEconomyNoMaf" );
	this.setFuelEconomyNoMaf();
	logger.debug("Calling setTimingAdvance" );
	this.setTimingAdvance();
	logger.debug("Calling setDtcCode" );
	this.setDtcCode();
	logger.debug("Calling setEquivRatio" );
	this.setEquivRatio();
	
	logger.debug("Refresh Done" );
	

}

public void init(SerialPort serialPort ) {
	this.serialPort = serialPort;
}


/**
 * @return the airIntakeTemp
 */
public float getAirIntakeTemp() {
	return AirIntakeTemp;
}


/**
 * @return the ambientAirTemp
 */
public float getAmbientAirTemp() {
	return AmbientAirTemp;
}


/**
 * @return the engineCoolantTemp
 */
public float getEngineCoolantTemp() {
	return EngineCoolantTemp;
}


/**
 * @return the barometricPressure
 */
public int getBarometricPressure() {
	return BarometricPressure;
}


/**
 * @return the fuelPressure
 */
public int getFuelPressure() {
	return FuelPressure;
}


/**
 * @return the intakeManifoldPressure
 */
public int getIntakeManifoldPressure() {
	return IntakeManifoldPressure;
}


/**
 * @return the engineLoad
 */
public float getEngineLoad() {
	return EngineLoad;
}


/**
 * @return the engineRuntime
 */
public String getEngineRuntime() {
	return engineRuntime;
}


/**
 * @return the engineRpm
 */
public int getEngineRpm() {
	return engineRpm;
}


/**
 * @return the speed
 */
public int getSpeed() {
	return speed;
}


/**
 * @return the maf
 */
public float getMaf() {
	return maf;
}


/**
 * @return the throttle
 */
public float getThrottle() {
	return throttle;
}


/**
 * @return the fuelLevel
 */
public float getFuelLevel() {
	return fuelLevel;
}


/**
 * @return the fuelType
 */
public String getFuelType() {
	return fuelType;
}


/**
 * @return the fuelConsumption
 */
public float getFuelConsumption() {
	return fuelConsumption;
}


/**
 * @return the fuelEconomy
 */
public float getFuelEconomy() {
	return fuelEconomy;
}


/**
 * @return the fuelEconomywithMaf
 */
public float getFuelEconomywithMaf() {
	return fuelEconomywithMaf;
}


/**
 * @return the fuelEconomyNoMaf
 */
public float getFuelEconomyNoMaf() {
	return fuelEconomyNoMaf;
}


/**
 * @return the timingAdvance
 */
public float getTimingAdvance() {
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
	return equivRatio;
}


/**
 * @param airIntakeTemp the airIntakeTemp to set
 */
private void setAirIntakeTemp() {
	try {
	airIntakeTemperatureCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
	AirIntakeTemp = airIntakeTemperatureCommand.getTemperature();
	} catch ( Exception e ) {
		AirIntakeTemp = -999;
		logger.debug("Error getting AirtakeTemp : {}", e.toString());
	}
}


/**
 * @param ambientAirTemp the ambientAirTemp to set
 */
private void setAmbientAirTemp() {
	try {
	 ambientAirTemperatureCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
	 this.AmbientAirTemp = ambientAirTemperatureCommand.getTemperature();
	} catch ( Exception e ) {
		this.AmbientAirTemp = -999;
		logger.debug("Error getting AmbientAirTemp : {}", e.toString());
	}
}


/**
 * @param engineCoolantTemp the engineCoolantTemp to set
 */
private void setEngineCoolantTemp() {
	try {
		 engineCoolantTemperatureCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		 EngineCoolantTemp = engineCoolantTemperatureCommand.getTemperature();
		 
		 logger.debug("Setting engineCoolantTemp to : {}", EngineCoolantTemp );
		} catch ( Exception e ) {
			this.EngineCoolantTemp = -999;
			logger.debug("Error getting AmbientAirTemp : {}", e.toString());
		}
}


/**
 * @param barometricPressure the barometricPressure to set
 */
private void setBarometricPressure() {
	BarometricPressure = -1;
}


/**
 * @param fuelPressure the fuelPressure to set
 */
private void setFuelPressure() {
	FuelPressure = -1;
}


/**
 * @param intakeManifoldPressure the intakeManifoldPressure to set
 */
private void setIntakeManifoldPressure() {
	try {
		intakeManifoldPressureCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		this.IntakeManifoldPressure = intakeManifoldPressureCommand.getMetricUnit();
	} catch (IOException | InterruptedException e) {
		logger.debug("Error getting IntakeManifoldPressure : {}", e.toString());
		this.IntakeManifoldPressure = -1;
		e.printStackTrace();
	}
}


/**
 * @param engineLoad the engineLoad to set
 */
private void setEngineLoad() {
	try {
		 engineLoadCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		 this.EngineLoad = engineLoadCommand.getPercentage();
		} catch ( Exception e ) {
			this.EngineLoad = -1;
			logger.debug("Error getting EngineLoad : {}", e.toString());
			
		}
	
}


/**
 * @param engineRuntime the engineRuntime to set
 */
private void setEngineRuntime() {
	this.engineRuntime = "";
}


/**
 * @param engineRpm the engineRpm to set
 */
private void setEngineRpm() {
	try {
		 engineRPMCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		 this.engineRpm = engineRPMCommand.getRPM();
	} catch ( Exception e ) {
		this.engineRpm = -1;
		logger.debug("Error getting RPM : {}", e.toString());
	}
}


/**
 * @param speed the speed to set
 */
private void setSpeed() {
	try {
	speedCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
	this.speed = speedCommand.getMetricSpeed();
	} catch (Exception e ) {
		this.speed = -1;
		logger.debug("Error getting Speed : {}", e.toString());
	}
}


/**
 * @param maf the maf to set
 */
private void setMaf() {
	this.maf = 0;
}


/**
 * @param throttle the throttle to set
 */
private void setThrottle() {
	try {
		throttlePositionCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		this.throttle = throttlePositionCommand.getPercentage();
	} catch (Exception e ) {
		this.throttle = -1;
		logger.debug("Error getting Throttle : {}", e.toString());
	}
}


/**
 * @param fuelLevel the fuelLevel to set fuelLevelCommand
 */
private void setFuelLevel() {
	try {
		fuelLevelCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		this.fuelLevel = fuelLevelCommand.getFuelLevel();
	} catch (Exception e ) {
		this.fuelLevel = -1;
		logger.debug("Error getting FuelLevel : {}", e.toString());
	}
}


/**
 * @param fuelType the fuelType to set
 */
private void setFuelType() {
	try {
		findFuelTypeCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		this.fuelType = findFuelTypeCommand.getFormattedResult();
	} catch (Exception e ) {
		this.fuelType = "ERROR";
		logger.debug("Error getting FuelType : {}", e.toString());
	}
}


/**
 * @param fuelConsumption the fuelConsumption to set
 */
private void setFuelConsumption() {
	try {
		fuelConsumptionCommand.run(serialPort.getInputStream(), serialPort.getOutputStream());
		if (fuelConsumptionCommand.isValid()) {
			this.fuelConsumption = fuelConsumptionCommand.getLitersPerHour();
		} else 
		{
			this.fuelConsumption = -1;
		}
	} catch (Exception e ) {
		this.fuelConsumption = -1;
		logger.debug("Error getting FuelConsumption : {}", e.toString());
	}
}


/**
 * @param fuelEconomy the fuelEconomy to set
 */
private void setFuelEconomy() {
	this.fuelEconomy = -1;
}


/**
 * @param fuelEconomywithMaf the fuelEconomywithMaf to set
 */
private void setFuelEconomywithMaf() {
	this.fuelEconomywithMaf = -1;
}


/**
 * @param fuelEconomyNoMaf the fuelEconomyNoMaf to set
 */
private void setFuelEconomyNoMaf() {
	this.fuelEconomyNoMaf = -1;
}


/**
 * @param timingAdvance the timingAdvance to set
 */
private void setTimingAdvance() {
	this.timingAdvance = -1;
}


/**
 * @param dtcCode the dtcCode to set
 */
private void setDtcCode() {
	this.dtcCode = "";
}


/**
 * @param equivRatio the equivRatio to set
 */
private void setEquivRatio() {
	this.equivRatio = -1;
}



		  
}
