package util.Enums;
/** @Author: Raveena Choudhary, 40232370 **/

public enum SlotEnum {
	MORNING("M"), EVENING("E"),AFTERNOON("A");

	private final String name;

	SlotEnum(String name) {
		this.name = name;
	}
	public String value(){
		return this.name;
	}

	public static String getEnumNameForValue(String val){
		for(SlotEnum slot : SlotEnum.values())
		{
			if(slot.value().equalsIgnoreCase(val)) {
				return slot.name();
			}
		}

		return "";
	}
}
