import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;


public class Driver {

    private static File srcFile;

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) {
            System.out.println("\nError: please provide the source file path");
            System.exit(1);
        }
        else if (args.length == 1) {
            srcFile = new File(args[0]);
            String fileExtension = srcFile.toString();
            int index = fileExtension.lastIndexOf(".");
            if (index == -1 || !fileExtension.substring(index).equals(".asm")) {
                System.out.println("\nError: please provide the file with .asm");
                System.exit(1);
            }

            if (!srcFile.exists()) {
                System.out.println("\nError: file doesn't exist");
                System.exit(1);
            }

        }
        else {
            System.out.println("Error: please provide only 1 argument");
            System.exit(1);
        }

        setTable(srcFile);


    }

    public static void setTable(File srcFile) throws FileNotFoundException {
        int size = count(srcFile);
        //set the size of the array by how many lines are in the file
        if (size == 0) {
            System.exit(1);
        }

        SicInstruction[] instructions = new SicInstruction[size];
        String[] temp;
        String line;
        String str;
        int i = 0;

        try(Scanner read = new Scanner(srcFile)) {
            while (read.hasNextLine()) {
                instructions[i] = new SicInstruction();
                line = read.nextLine();
                str = line.trim();
                //regex to split any white space including \n \t and " "
                temp = str.split("\\s+");
                //for instructions without label
                if (temp.length == 2) {
                    if (temp[0].equals("END")) {
                        instructions[i].setSymbol("\t");
                    } else {
                        instructions[i].setSymbol("$\t");
                    }

                    instructions[i].setInstruction(temp[0]);
                    instructions[i].setReference(temp[1]);
                    i++;
                }
                //for instructions with no label and operand
                else if (temp.length == 1) {
                    instructions[i].setSymbol("$\t");
                    instructions[i].setInstruction(temp[0]);
                    instructions[i].setReference("");
                    i++;
                }
                else {
                    instructions[i].setSymbol(temp[0]);
                    instructions[i].setInstruction(temp[1]);
                    instructions[i].setReference(temp[2]);
                    i++;
                }

            }
        }

        //prints the splitted string in label, opcode and operand format
        printTable(instructions);


        pass1(instructions);


    }

    //counts how many lines are in the file/ how many instructions
    public static int count(File srcFile) throws FileNotFoundException {
        int count = 0;

        try (Scanner input = new Scanner(srcFile)) {
            while (input.hasNextLine()) {
                count++;
                input.nextLine();
            }
        }

        return count;
    }

    public static void printTable(SicInstruction[] instructions) {

        for (SicInstruction instruction : instructions) {
            System.out.println(instruction.getSymbol() + " " + instruction.getInstruction() + " " + instruction.getReference());
        }
    }

    public static void printTableA(SicInstruction[] instructions) {

        for (SicInstruction instruction : instructions) {
            System.out.println(instruction.getAddress() + " " + instruction.getSymbol() + " " + instruction.getInstruction() + " " + instruction.getReference());
        }
    }

    public static void pass1(SicInstruction[] instruction) {

        //reads the header and grabs the address of the first executable instruction
        // and converts it from hex string to a decimal value
        int loc = Integer.parseInt(instruction[0].getReference(), 16);

        //started from i = 1 because i = 0 contains the prog name
        // and starting address in the SicInstruction Array
        for (int i = 1; i < instruction.length; i++) {
            switch (instruction[i].getInstruction()) {
                case "RESW":
                    instruction[i].setAddress(Integer.toHexString(loc));
                    loc += Integer.parseInt(instruction[i].getReference()) * 3;
                    break;
                case "RESB":
                    instruction[i].setAddress(Integer.toHexString(loc));
                    loc += Integer.parseInt(instruction[i].getReference());
                    break;
                case "BYTE":
                    instruction[i].setAddress(Integer.toHexString(loc));
                    if (instruction[i].getReference().startsWith("C")) {
                        int eIndex = instruction[i].getReference().lastIndexOf('\'') - 1;
                        String str = instruction[i].getReference().substring(1, eIndex);
                        loc += str.length();
                    } else if (instruction[i].getReference().startsWith("X")) {
                        int eIndex = instruction[i].getReference().lastIndexOf('\'') - 1;
                        String str = instruction[i].getReference().substring(1, eIndex);
                        loc += str.length() / 2;

                    }
                    break;
                default:
                    instruction[i].setAddress(Integer.toHexString(loc));
                    loc += 3;
            }


        }

        printTableA(instruction);
        System.out.println("----- SymTable------");
        constructPrintSymbolTable(instruction);

    }

    public static void constructPrintSymbolTable(SicInstruction[] instructions) {

        HashMap<String, String> symbolTable = new HashMap<>();


//        ArrayList<SymbolTable> table = new ArrayList<>();

//        table.add(new SymbolTable(instructions[0].getReference(), instructions[0].getSymbol()));
        symbolTable.put(instructions[0].getSymbol(), instructions[0].getReference());

        for (int i = 1; i < instructions.length; i++) {

            if (instructions[i].getSymbol().equals("$\t") || instructions[i].getInstruction().equals("END")) {
                continue;
            }
//            table.add(new SymbolTable(instructions[i].getAddress(), instructions[i].getSymbol()));
            symbolTable.put(instructions[i].getSymbol(), instructions[i].getAddress());
        }

//        for (SymbolTable symbolTable : table) {
//            System.out.println(symbolTable.getAddress() + " " + symbolTable.getLabel());
//        }

        System.out.println(symbolTable);

        pass2(symbolTable, instructions);


    }

    public static void pass2(HashMap<String, String> symbolTable, SicInstruction[] instructions) {

        String objCode = "";
        for (int i = 1; i < instructions.length; i++) {

            //ignore if the directive is RESW or RESB becuase they have no object code
            if (instructions[i].getInstruction().equals("RESW") || instructions[i].getInstruction().equals("RESB") || instructions[i].getInstruction().equals("END")) {
                instructions[i].setObjCode("?");
                continue;
            }

            //zero operand instruction. concat the opcode with 4 hex zeros
            else if (instructions[i].getReference().equals("")) {
                objCode = OpcodeTable.getOpcode(instructions[i].getInstruction()) + "0000";
                instructions[i].setObjCode(objCode);

            }

            else if (instructions[i].getInstruction().equals("BYTE")) {
                objCode = "";
                //if the operand of the directive starts with C
                //index start at 2 because 0 and 1 belongs to C and ' respectively
                //and ends at length - 1 because the last index contains '
                if (instructions[i].getReference().startsWith("C")) {
                    for (int j = 2; j < instructions[i].getReference().length() - 1; j++) {
                        //converts it to a ASCII hex representation
                        objCode += Integer.toHexString(instructions[i].getReference().charAt(j));
                    }
                    instructions[i].setObjCode(objCode);
                }
                else {
                    //index start at 2 because 0 and 1 belongs to X and ' respectively
                    for (int j = 2; j < instructions[i].getReference().length() - 1; j++) {
                        objCode += instructions[i].getReference().charAt(j);
                    }
                    instructions[i].setObjCode(objCode);
                }

            }

            else if (instructions[i].getInstruction().equals("WORD")) {
                objCode = "";
                //converts word from decimal to a hex representation
                String hex = Integer.toHexString(Integer.parseInt(instructions[i].getReference()));
                //if word value is less than 6 nibbles/ 3 bytes we keep concatenating 0 to the string
                //then we concatenate the word value at the end
                int len =  hex.length();
                while (len < 6) {
                    objCode += "0";
                    len++;
                }
                objCode += hex;


                instructions[i].setObjCode(objCode);

            }
            else {
                //if indexed addressing is used
                if (instructions[i].getReference().endsWith(",X")) {
                    int len = instructions[i].getReference().length() - 2;
                    objCode = OpcodeTable.getOpcode(instructions[i].getInstruction());
                    int temp = 0x8000 + Integer.parseInt(symbolTable.get(instructions[i].getReference().substring(0, len)),16);
                    objCode += Integer.toHexString(temp);
                    instructions[i].setObjCode(objCode);
                    continue;
                }
                objCode = OpcodeTable.getOpcode(instructions[i].getInstruction());
                objCode += symbolTable.get(instructions[i].getReference());
                instructions[i].setObjCode(objCode);
            }


        }


        for (SicInstruction test : instructions) {
                System.out.println(test.getObjCode());

        }

        printHTE(instructions);

    }

    public static void printHTE(SicInstruction[] instruction) {

        //H record
        String h = "H";
        String progName = instruction[0].getSymbol();
        String firstInstAdH = instruction[1].getAddress();
        String finalInstAdH = instruction[instruction.length -1].getAddress();
        String lenH = Integer.toHexString(Integer.parseInt(finalInstAdH, 16) - Integer.parseInt(firstInstAdH, 16));

        firstInstAdH = formatCol6(firstInstAdH);
        lenH = formatCol6(lenH);

        h += " " + progName + " " + firstInstAdH + " "+ lenH;
        System.out.println(h);

        //T record

        String t;
        String firstInstAdT;
        int i = 1;
        int j = 1;

        while (j < instruction.length ) {
//            System.out.println(j);
            String objT = "";
            firstInstAdT = formatCol6(instruction[i].getAddress());


            int b = 0;
            while (b < 30) {

                if (instruction[i].getInstruction().equals("RESW") || instruction[i].getInstruction().equals("RESB")) {
                    i++;
                    break;
                }
                if (instruction[i].getInstruction().equals("END")) {
                    break;
                }
                if (objT.length() + instruction[i].getObjCode().length() > 70) {
                    break;
                }


                objT += instruction[i].getObjCode() + " ";

                if (instruction[i].getInstruction().equals("WORD")) {
                    b += 3;
                }
                else if (instruction[i].getInstruction().equals("BYTE")) {
                    String temp = instruction[i].getReference();

                    int byt;
                    if (temp.charAt(0) == 'C') {
                        byt = temp.length() - 3;

                    } else {
                        byt = (temp.length() - 3) / 2;
                    }
                    b += byt;

                }
                else {

                    b += 3;
                }


                i++;
            }
            if(b != 0){
                t = "T " ;
                String lenT = Integer.toHexString(b);
                lenT = formatCol2(lenT);
//                System.out.println(objT.length());

                t += firstInstAdT + " "+ lenT + " "+ objT;

                System.out.println(t);
            }
//            System.out.println(b);
            j++;

        }



        //E record
        String e = "E";
        String firstInstAdE = firstInstAdH;
        e += " " + firstInstAdE;

        System.out.println(e);

    }

    public static String formatCol6(String col) {

        String temp ="";
        for (int i = col.length(); i < 6; i++) {
            temp += 0;
        }
        temp += col;
        return temp;
    }

    public static String formatCol2(String col) {

        String temp ="";
        for (int i = col.length(); i < 2; i++) {
            temp += 0;
        }
        temp += col;
        return temp;
    }

}
