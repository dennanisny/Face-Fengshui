package com.facefengshui.two;

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
		if (eyeDistance > 48) {
			results.put("Tolerance", "Open-minded: Your eye distance to face width ratio is more than 48%");
		}
		else {
			results.put("Tolerance", "Detail-oriented: Your eye distance to face width ratio is no more than 48%");
		}
	}
	
	void setMouthSize(double mouthSize) {
		if (mouthSize > 45) {
			results.put("Generosity", "Generous: Your mouth size to face width ratio is more than 45%.");
		}
		else {
			results.put("Generosity", "Purposeful: Your mouth size to face width ratio is no more than 45%.");
		}
	}
	
	void setPhiltrumLength(double philtrumLength) {
		if (philtrumLength > 8) {
			results.put("Health", "Good: Your philtrum length to face height ratio is more than 8%.");
		}
		else {
			results.put("Health", "Decent: Your philtrum length to face height ratio is no more than 8%.");
		}
	}
}
