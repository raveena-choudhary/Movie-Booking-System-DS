/** @Author: Raveena Choudhary, 40232370 **/
package util.Enums;

public enum ServerEnum {

    ATWATER("ATW"), VERDUN("VER"),OUTREMONT("OUT");

    private final String serverName;

    ServerEnum(String serverName) {
        this.serverName = serverName;
    }
    public String value(){
        return this.serverName;
    }

    public static String getEnumNameForValue(String val){
            for(ServerEnum location : ServerEnum.values())
            {
                if(location.value().equalsIgnoreCase(val)) {
                    return location.name();
                }
            }

        return "";
    }
}
