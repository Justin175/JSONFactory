package j175.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 * @author Justin Treulieb
 * @version 1.0
 * @since v1.0
 */
public class JSONBuilder {

	private String json;
	private JSONObject out;
	private String errorMessage;
	private int index;
	
	public JSONBuilder(String json) {
		this.json = json;
		this.index = -1;
	}
	
	private JSONBuilder(String json, int index) {
		this.json = json;
		this.index = index;
	}
	
	public String getJSONString() {
		return json;
	}
	
	public JSONObject getJSON() {
		return out;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isFailed() {
		return errorMessage != null;
	}
	
	public void buid() {
		try { 
			_build();	
		}
		catch(Exception e) {
			errorMessage = e.getMessage();
		}
	}
	
	private void _build() {
		JSONObject head = new JSONObject();
		
		char currentChar;
		String currentKey = null;
		
		while(++index < json.length()) {
			//get next char
			currentChar = json.charAt(index);
			
			//Fall 1: Ignoriere \n
			if(currentChar == '\n' || currentChar == '\r' || currentChar == '\t')
				continue;
			
			//Fall 2: Liest ein '"' ein und hat besiher kein key eingelesen
			else if(currentChar == '"' && currentKey == null) {
				currentKey = readString();
				
				//Nachdem der key eingelesen wurde, wird nach einer value gesucht
				//Suche nach beginn der value mittels ':'
				
				while(++index < json.length() && (currentChar = json.charAt(index)) != ':');
				
				//Hier k�nnen 4 F�lle eintreten:
				// 1: String ('"')
				// 2: Zahl ('[+-[0-9]]')
				// 3: Array ('[')
				// 4: JSON ('{')
				
				//Suche nach einem Indikator f�r eine von den vier F�llen
				while(++index < json.length()) {
					currentChar = json.charAt(index);
					
					//Fall 1: Suche nach '"'
					if(currentChar == '"') {
						//Lese den String
						String value = readString();
						
						//F�ge neues Attribut hinzu
						head.addAttribute(currentKey, value);
						break;
					}
					
					//Fall 2: Suche nach [.+-0-9]
					else if(currentChar == '.' || currentChar == '+' || currentChar == '-' || 
							(currentChar >= '0' && currentChar <= '9')) {
						
						//lese die Zahl ein
						NumberReadInformation info = readNumber();
						
						if(info.failed)
							throw new ConvertException("'" + info.numberString + "' is not a number.");
						
						//F�ge neues Attribut hinzu
						head.addAttribute(currentKey, info.number);
						index--;
						break;
					}
					
					//Fall 3: Suche nach '['
					else if(currentChar == '[') {
						//add Attr
						Object[] arr = readList(currentKey);
						head.addAttribute(currentKey, arr);
						break;
					}
					
					//Fall 4: Suche nach '{'
					else if(currentChar == '{') {
						//Erstelle neuen Builder
						JSONBuilder builder = new JSONBuilder(this.json, index);
						builder.buid();
						
						if(!builder.isFailed()) {
							head.addAttribute(currentKey, builder.getJSON());
							this.index = builder.index;
							break;
						}
						else
							System.err.println("Builder Failed at key '" + currentKey + "': " + builder.errorMessage);
					}
				}
			}
			
			//n�chstes Key-Value-Paar
			//-> derzeitigen key auf null setzen
			else if(currentChar == ',') {
				currentKey = null;
			}
			
			// schlie�ende Klammer wird gefunde > break
			else if(currentChar == '}') {
				break;
			}
		}
		
		this.out = head;
	}
	
	private Object[] readList(String currentKey) {
		LinkedList<Object> list = new LinkedList<>();
		char currentChar;
		
		while(++index < json.length()) {
			currentChar = json.charAt(index);
			
			//Ein Array besteht aus endlich vielen Elementen, welche
			//jedoch nicht den selben Typen aufweisen müssen
			//Vier typen existieren.
			//1. String
			//2. Zahl
			//3. JSON-Object (Rekursiver Aufrunf)
			//4. Array (Rekursiver Aufruf)
			
			//Fall 1
			if(currentChar == '"') {
				String content = readString();
				
				//Add content to list
				list.add(content);
			}
			
			//Fall 2
			else if(currentChar == '.' || currentChar == '+' || currentChar == '-' || 
					(currentChar >= '0' && currentChar <= '9')) {
				NumberReadInformation info = readNumber();
				
				if(info.failed)
					throw new ConvertException("'" + info.numberString + "' is not a number.");
				
				list.add(info.number);
				this.index--;
			}
			
			//Fall 3
			else if(currentChar == '{') {
				JSONBuilder builder = new JSONBuilder(this.json, index);
				builder.buid();
				
				if(!builder.isFailed()) {
					list.add(builder.getJSON());
					this.index = builder.index;
				}
				else
					System.err.println("Builder Failed while Building JSON-Object in Array at Key '" + currentKey + "': " + builder.errorMessage);
			}
			
			//Fall 4
			else if(currentChar == '[') {
				//read next list
				Object[] content = readList(currentKey);
				
				//Add to list
				list.add(content);
			}
			
			//end list
			else if(currentChar == ']') {
				break;
			}
		}
		
		return list.toArray();
	}
	
	private String readString() {
		StringBuilder builder = new StringBuilder(20);
		boolean isBackslashBefore = false;
		String ret = null;
		char currentChar;
		
		//lese den Key ein
		while(++index < json.length()) {
			currentChar = json.charAt(index);
			
			//�berpr�fe auf ende
			if(currentChar == '\\' && !isBackslashBefore) {
				isBackslashBefore = true;
				continue;
			}
			else if(currentChar == '"' && !isBackslashBefore) {
				ret = builder.toString();
				break;
			}
			
			//add to key-string
			else builder.append(currentChar);
			
			//reset
			isBackslashBefore = false;
		}
		
		return ret;
	}

	private NumberReadInformation readNumber() {
		StringBuilder builder = new StringBuilder(15);
		char currentChar = json.charAt(index);
		
		boolean isDecimal = false;
		
		//first step do manually
		if(currentChar == '-' || currentChar == '+') {
			//search for beginning of number
			builder.append(currentChar);
			while(++index < json.length() && (currentChar = json.charAt(index)) == ' ');
		}
		
		//go one back
		index--;
		
		while(++index < json.length()) {
			currentChar = json.charAt(index);
			
			//ignore new line
			if(currentChar == '\n') 
				continue;
			
			if(currentChar == '.' || currentChar == 'e' || currentChar == 'E') {
				isDecimal = true;
			}
			//object ist nun zuende
			else if(currentChar == ',' || currentChar == '}' || currentChar == ']') {
				break;
			}
			
			builder.append(currentChar);
		}
		
		//Convert to number
		NumberReadInformation info = new NumberReadInformation();
		info.numberString = builder.toString().trim();
		
		//Decimal
		if(isDecimal) {
			try {
				BigDecimal number = new BigDecimal(info.numberString);
				info.number = number;
			} catch(Exception e) {
				e.printStackTrace();
				info.failed = true;
			}
		}
		//Integer
		else {
			try {
				BigInteger number = new BigInteger(info.numberString);

				//try to convert number to int or long
				int bitCount = number.bitCount();
				int bitLength = number.bitLength();
				Object numberOut;
				
				if(bitCount != bitLength) { //negativ
					if(bitLength <= 32) {
						numberOut = number.intValue();
					}
					else if(bitLength <= 64) {
						numberOut = number.longValue();
					}
					else numberOut = number;
				}
				else { //positiv
					if(bitLength < 32) {
						numberOut = number.intValue();
					}
					else if(bitLength < 64) {
						numberOut = number.longValue();
					}
					else numberOut = number;
				}
				
				info.number = numberOut;
			} catch(Exception e) {
				e.printStackTrace();
				info.failed = true;
			}
		}
		
		return info;
	}
	
	private class NumberReadInformation {
		public boolean failed;
		public Object number;
		public String numberString;
		
		public NumberReadInformation() {}
	}
}
