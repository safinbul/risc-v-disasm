import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java Main <elf-file> <output-file>");
            return;
        }
        Elf32Parser parser = new Elf32Parser(args[0]);
        parser.parse();
        PrintWriter out = new PrintWriter(args[1]);

        out.println(".text");
        LinkedHashMap<Integer, Integer> textSection = parser.getTextSection(out);
        disassemble(textSection, parser.getMarks(), out);
        out.println("\n.symtab");
        parser.printSymtab(out);
        out.close();
    }

    public static void disassemble(LinkedHashMap<Integer, Integer> textSection, HashMap<Integer, String> marks,
            PrintWriter out) {
        InstructionManager manager = new InstructionManager(marks);
        for (Map.Entry<Integer, Integer> entry : textSection.entrySet()) {
            int address = entry.getKey();
            int instruction = entry.getValue();
            manager.addInstruction(address, instruction);
        }
        out.println(manager.toPrint());
    }
}