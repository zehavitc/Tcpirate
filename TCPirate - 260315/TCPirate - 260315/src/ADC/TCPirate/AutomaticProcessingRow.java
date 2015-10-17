package ADC.TCPirate;

import com.sun.org.apache.bcel.internal.generic.SWITCH;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedHashMap;

/**
 * Created by zehavitc on 6/16/2015.
 */
public class AutomaticProcessingRow {

    public enum Actions {Read,Modify}
    public enum Functions {Or,And,Plus,Custom}
    public String packetFilter;
    public String fieldOffset;
    public String fieldLength;
    public String fieldBaseString;
    public Actions action;
    public Functions function;
    public String variable;
    public String functionInput;
    public String filterFunction;
    public Method filterFunctionInstance;
    public Method functionInstance;

    public AutomaticProcessingRow(){
        packetFilter = "";
        filterFunction="";
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
            case 1:return filterFunction;
            case 2:return fieldOffset;
            case 3: return  fieldLength;
            case 4: return fieldBaseString;
            case 5: return action.toString();
            case 6: return variable;
            case 7: return function.toString();
            case 8: return  functionInput;
            default:return "";
        }
    }

    public void set(int columnIndex, String value) {
        switch (columnIndex) {
            case 0:
                packetFilter = value;
                return;
            case 1:
                filterFunction = value;
                return;
            case 2:
                fieldOffset = value;
                return;
            case 3:
                fieldLength = value;
                return;
            case 4:
                fieldBaseString = value;
                return;
            case 5:
                action = Actions.valueOf(value);
                return;
            case 6:
                variable = value;
                return;
            case 7:
                function = Functions.valueOf(value);
                return;
            case 8:
                functionInput = value;
        }

    }

    public boolean isCustomFunction(){
        return action.equals(Actions.Modify.toString()) && function.equals(Functions.Custom.toString());
    }


    public void loadFunctions(Class jarClass){
        try {
            if (filterFunction != null && !filterFunction.isEmpty()) {
                filterFunctionInstance = jarClass.getDeclaredMethod(filterFunction, byte[].class,int.class, boolean.class, ArrayList.class);
            }
            if (isCustomFunction()) {
                functionInstance = jarClass.getDeclaredMethod(functionInput, byte[].class, boolean.class, ArrayList.class, LinkedHashMap.class);
            }
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}







