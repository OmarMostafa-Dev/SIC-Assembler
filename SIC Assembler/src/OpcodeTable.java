import java.util.HashMap;


public class OpcodeTable {

    private static final HashMap<String, String> opcode;

    static {
        opcode = new HashMap<>();
        opcode.put("ADD", "18");
        opcode.put("AND", "40");
        opcode.put("COMP", "28");
        opcode.put("DIV", "24");
        opcode.put("J", "3c");
        opcode.put("JEQ", "30");
        opcode.put("JGT", "34");
        opcode.put("JLT", "38");
        opcode.put("JSUB", "48");
        opcode.put("LDA", "00");
        opcode.put("LDCH", "50");
        opcode.put("LDL", "08");
        opcode.put("LDX", "04");
        opcode.put("MUL", "20");
        opcode.put("OR", "44");
        opcode.put("RD", "d8");
        opcode.put("RSUB", "4c");
        opcode.put("TD", "e0");
        opcode.put("TIX", "2c");
        opcode.put("WD", "dc");
        opcode.put("STA", "0c");
        opcode.put("STCH", "54");
        opcode.put("STL", "14");
        opcode.put("STSW", "e8");
        opcode.put("STX", "10");
        opcode.put("SUB", "1c");
    }


    public static String getOpcode(String key) {
        return opcode.get(key);
    }

}
