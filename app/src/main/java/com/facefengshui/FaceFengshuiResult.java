package com.facefengshui;

import java.io.Serializable;
import java.util.Hashtable;

@SuppressWarnings("serial")
class FaceFengshuiResult implements Serializable {
	private Hashtable<String, String> results = new Hashtable<>();
	Hashtable<String, String> getResults()
	{
		return results;
	}

	void setEyeDistance(double eyeDistance) {
		if (eyeDistance > 50) {
			results.put("Tolerance", "Open-minded");
		}
		else {
			results.put("Tolerance", "Detail-oriented");
		}
	}
	
	void setMouthSize(double mouthSize) {
		if (mouthSize > 40) {
			results.put("Generosity", "Generous");
		}
		else {
			results.put("Generosity", "Purposeful");
		}
	}
	
	void setPhiltrum(double philtrum) {
		if (philtrum > 25) {
			results.put("Health", "Good");
		}
		else {
			results.put("Health", "Decent");
		}
	}
	
	void setChinWidth(double chinWidth) {
		if (chinWidth > 20) {
			results.put("Career", "Career-focused");
		}
		else {
			results.put("Career", "Family-focused");
		}
	}
	
	void setForeheadSize(double foreheadSize) {
		if (foreheadSize > 25) {
			results.put("Learning Pattern", "Traditional");
		}
		else {
			results.put("Learning Pattern", "Experience-oriented");
		}
	}
}
