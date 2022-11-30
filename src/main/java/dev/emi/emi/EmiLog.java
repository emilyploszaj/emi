package dev.emi.emi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmiLog {
	public static final Logger LOG = LoggerFactory.getLogger("EMI");
	
	public static void info(String str) {
		LOG.info(str);
	}
	
	public static void warn(String str) {
		LOG.warn(str);
	}
	
	public static void error(String str) {
		LOG.error(str);
	}
}
