package j175.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Justin Treulieb
 * @version 1.0
 * @since v1.0
 */
public final class JSONFactory {

	/**
	 * Builds a JSON-Object from the JSON-String representation
	 * 
	 * @param jsonString JSON-Strin
	 * @return JSON-Object if jsonString is valid
	 */
	public static JSONObject toJSON(String jsonString) {
		JSONBuilder builder = new JSONBuilder(jsonString);
		builder.buid();
		
		if(builder.isFailed()) {
			System.err.println("Builder Failed.");
			System.err.println("Error-Message: " + builder.getErrorMessage());
			return null;
		}
		
		return builder.getJSON();
	}
	
	/**
	 * Builds a JSON-Object from the JSON-File representation
	 * 
	 * @param jsonString JSON-Strin
	 * @return JSON-Object if jsonString is valid
	 * 
	 * @throws IOException See {@link FileReader}
	 */
	public static JSONObject toJSON(File jsonFile) throws IOException {
		JSONBuilder builder = new JSONBuilder(readFile(jsonFile));
		builder.buid();
		
		if(builder.isFailed()) {
			System.err.println("Builder Failed.");
			System.err.println("Error-Message: " + builder.getErrorMessage());
			return null;
		}
		
		return builder.getJSON();
	}
	
	private static String readFile(File file) throws IOException {
		StringBuilder builder = new StringBuilder(150);
		
		try(FileReader r = new FileReader(file)){
			int chr;
			
			while((chr = r.read()) != -1)
				builder.append((char) chr);
		}
		
		return builder.toString();
	}
}
