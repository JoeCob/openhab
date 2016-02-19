package org.openhab.binding.geolocation.internal;

import org.json.JSONObject;
import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoLocation {

	/*{"place_id":"63809788","licence":"Data © OpenStreetMap contributors, ODbL 1.0. http:\/\/www.openstreetmap.org\/copyright",
	 * "osm_type":"way","osm_id":"35763510","lat":"-29.942885","lon":"-50.9152072",
	 * "display_name":"Freeway, Gravataí, Microregion of Porto Alegre, Metropolitan Region of Porto Alegre, Metropolitan Mesoregion of Porto Alegre, Rio Grande do Sul, South Region, Brazil",
	 * "address":{"road":"Freeway","city":"Gravataí","county":"Microregion of Porto Alegre",
	 * 	"state_district":"Metropolitan Mesoregion of Porto Alegre","state":"Rio Grande do Sul",
	 * 	"country":"Brazil","country_code":"br"},
	 *  "extratags":{"lanes":"3","oneway":"yes","surface":"paved","maxspeed":"110"}}
	 */
	
	private int place_id =0;
	private int osm_id =0;
	private String osm_type = " ";
	private String display_name = " ";
	private String road_name = " ";
	private String postcode = " ";
	private String suburb = " ";
	private String city = " ";
	private String county = " ";
	private String state_district = " ";
	private String state = " ";
	private String country = " ";
	private String country_code = " ";
	private int lanes = 0;
	private int house_number = 0;
	private boolean oneway = false;
	private String surface_type = " ";
	private int maxspeed = 0;
	
	private static final Logger logger = LoggerFactory
			.getLogger(GeoLocationBinding.class);
	
	
	public int getPlace_id() {
		return place_id;
	}
	public void setPlace_id(int place_id) {
		this.place_id = place_id;
	}
	public int getOsm_id() {
		return osm_id;
	}
	public void setOsm_id(int osm_id) {
		this.osm_id = osm_id;
	}
	public String getOsm_type() {
		return osm_type;
	}
	public void setOsm_type(String osm_type) {
		this.osm_type = osm_type;
	}
	public String getDisplay_name() {
		return display_name;
	}
	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}
	public String getRoad_name() {
		return road_name;
	}
	public void setRoad_name(String road_name) {
		this.road_name = road_name;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public String getState_district() {
		return state_district;
	}
	public void setState_district(String state_district) {
		this.state_district = state_district;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCountry_code() {
		return country_code;
	}
	public void setCountry_code(String country_code) {
		this.country_code = country_code;
	}
	public int getLanes() {
		return lanes;
	}
	public void setLanes(int lanes) {
		this.lanes = lanes;
	}
	public boolean isOneway() {
		return oneway;
	}
	public void setOneway(boolean oneway) {
		this.oneway = oneway;
	}
	public String getSurface_type() {
		return surface_type;
	}
	public void setSurface_type(String surface_type) {
		this.surface_type = surface_type;
	}
	public int getMaxspeed() {
		return maxspeed;
	}
	public void setMaxspeed(int maxspeed) {
		this.maxspeed = maxspeed;
	}
	
	
	@Override
	public String toString() { 
		return "Street " + this.getRoad_name() + " City: " + this.getCity() + " MaxSpeed: " + this.maxspeed;
	}
	
	public void parseJson ( JSONObject location ) {
		
		try { 
			logger.debug("Parsing JSON - {}", location.toString());
			JSONObject address = location.getJSONObject("address");
			JSONObject extratags =  location.getJSONObject("extratags");
			if (address.has("city")) { 
				this.setCity(address.getString("city"));
			}
			this.setOsm_type(location.getString("osm_type"));
			if (address.has("contry")) { 
				this.setCountry(address.getString("contry"));
			}
			if (address.has("road")) {
				this.setRoad_name(address.getString("road"));
			}
			if (address.has("postcode")) {
				this.setPostcode(address.getString("postcode"));
			}
			if (address.has("county")) {
				this.setCounty(address.getString("county"));
			}
			
			if (address.has("house_number")) {
				this.setCounty(address.getString("house_number"));
			}
			
			if (address.has("suburb")) {
				this.setSuburb(address.getString("suburb"));
			}
			if (extratags.has("lanes")) {
				this.setLanes(extratags.getInt("lanes"));
			}
			if (extratags.has("maxspeed")) {
				this.setMaxspeed(extratags.getInt("maxspeed"));
			} else { 
				this.setMaxspeed( 0 );
			}
			
			if (location.has("display_name")) { 
				this.setDisplay_name(location.getString("display_name"));
			}
			
			logger.debug("locationDoc is now - {}", this.toString());
		} catch (Exception e)
		{
			//logger.error ("Exception parsing OpenMap location doc. Error was {} for item {}", e.toString());
			logger.error("Error parsing JSON into location. Error was {}", e.toString());
			e.printStackTrace();
		}

	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public String getSuburb() {
		return suburb;
	}
	public void setSuburb(String suburb) {
		this.suburb = suburb;
	}
	public int getHouse_number() {
		return house_number;
	}
	public void setHouse_number(int house_number) {
		this.house_number = house_number;
	}
	
}
