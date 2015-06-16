package ADC.TCPirate;

import com.sun.org.apache.bcel.internal.generic.SWITCH;

import java.util.Dictionary;

/**
 * Created by zehavitc on 6/16/2015.
 */
public class AutomaticProcessingRow {

    public enum Actions {Read,Write,Modify}
    public enum Functions {Or,And,Plus,Custom}
    public String packetFilter;
    public String fieldOffset;
    public String fieldLength;
    public String fieldBaseString;
    public Actions action;
    public Functions function;
    public String variable;
    public String functionInput;

    public AutomaticProcessingRow(){
        packetFilter = "";
        fieldOffset = "";
        fieldLength = "";
        fieldBaseString = "";
        action = Actions.Read;
        function = Functions.Or;
        variable = "";
        functionInput = "";
    }

    public String get(int columnIndex){
        switch (columnIndex){
            case 0:return packetFilter;
            case 1:return fieldOffset;
            case 2: return  fieldLength;
            case 3: return fieldBaseString;
            case 4: return action.toString();
            case 5: return variable;
            case 6: return function.toString();
            case 7: return  functionInput;
            default:return "";
        }
    }

    public void set(int columnIndex, String value) {
        switch (columnIndex) {
            case 0:
                packetFilter = value;
                return;
            case 1:
                fieldOffset = value;
                return;
            case 2:
                fieldLength = value;
                return;
            case 3:
                fieldBaseString = value;
                return;
            case 4:
                action = Actions.valueOf(value);
                return;
            case 5:
                variable = value;
                return;
            case 6:
                function = Functions.valueOf(value);
                return;
            case 7:
                functionInput = value;
        }

    }
}







