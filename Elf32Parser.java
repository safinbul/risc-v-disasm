import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Elf32Parser {
    public Elf32Header header;
    public ProgramHeader[] programHeaders;
    public SectionHeader[] sectionHeaders;

    private Map<String, SectionHeader> sections;

    public SectionHeader symtab;
    ByteBuffer buffer;

    Symbol[] symbols;

    byte[] strtab;

    public Elf32Parser(String pathname) throws IOException {
        File file = new File(pathname);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fis.read(bytes);
        fis.close();

        this.buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        this.sections = new LinkedHashMap<>();
    }

    public void parse() {

        Elf32Header header = new Elf32Header(buffer);
        sectionHeaders = new SectionHeader[header.e_shnum];
        parseSectionHeaders(header);

        SectionHeader strtabHeader = sectionHeaders[header.e_shstrndx];

        // parse section names
        byte[] names = parseStrtab(strtabHeader);
        for (int i = 0; i < header.e_shnum; i++) {
            int nameStart = sectionHeaders[i].sh_name;
            StringBuilder name = new StringBuilder();
            for (int j = nameStart; names[j] != 0; j++) {
                name.append((char) names[j]);
            }
            sections.put(name.toString(), sectionHeaders[i]);
        }

        this.strtab = parseStrtab(sections.get(".strtab"));
    }

    public void printSymtab(PrintWriter out) {
        if (symbols == null) {
            parseSymTab();
        }
        out.println("\nSymbol Value              Size Type     Bind     Vis       Index Name");
        for (int i = 0; i < symbols.length; i++) {
            Symbol symbol = symbols[i];
            out.printf("[%4d] 0x%-15X %5d %-8s %-8s %-8s %6s %s\n", i, symbol.st_value, symbol.st_size,
                    symbol.getType(),
                    symbol.getBind(), symbol.getVis(), symbol.getIndex(), symbol.getName());
        }

    }

    public void parseSectionHeaders(Elf32Header header) {
        buffer.position(header.e_shoff);
        for (int i = 0; i < header.e_shnum; i++) {
            SectionHeader sectionHeader = new SectionHeader(buffer);
            sectionHeaders[i] = sectionHeader;
            if (sectionHeader.sh_type == SectionHeader.SHT_SYMTAB) {
                symtab = sectionHeader;
            }
        }
    }

    public void parseSymTab() {
        if (symtab == null) {
            throw new RuntimeException("symtab not found");
        }
        symbols = new Symbol[symtab.sh_size / 16];
        for (int i = 0; i < symtab.sh_size / 16; i++) {
            buffer.position(symtab.sh_offset + i * 16);
            Symbol symbol = new Symbol(buffer);
            symbols[i] = symbol;
            int nameStart = symbol.st_name;
            StringBuilder name = new StringBuilder();
            for (int j = nameStart; strtab[j] != 0; j++) {
                name.append((char) strtab[j]);
            }
            symbol.setName(name.toString());
        }
    }

    public void parseSection(SectionHeader sectionHeader) {
        if (sectionHeader.sh_type != SectionHeader.SHT_STRTAB) {
            return;
        }
        buffer.position(sectionHeader.sh_offset);
        byte[] bytes = new byte[sectionHeader.sh_size];
        buffer.get(bytes);
        System.out.println(new String(bytes));
    }

    private byte[] parseStrtab(SectionHeader sectionHeader) {
        buffer.position(sectionHeader.sh_offset);
        byte[] bytes = new byte[sectionHeader.sh_size];
        buffer.get(bytes);
        return bytes;
    }

    public LinkedHashMap<Integer, Integer> getTextSection(PrintWriter out) {
        LinkedHashMap<Integer, Integer> instructions = new LinkedHashMap<>();
        SectionHeader text = sections.get(".text");
        buffer.position(text.sh_offset);
        byte[] bytes = new byte[text.sh_size];
        buffer.get(bytes);
        for (int i = 0; i < bytes.length; i += 4) {
            int instruction = (bytes[i] & 0xFF) | (bytes[i + 1] & 0xFF) << 8 | (bytes[i + 2] & 0xFF) << 16
                    | (bytes[i + 3] & 0xFF) << 24;
            instructions.put(text.sh_addr + i, instruction);
        }
        return instructions;
    }

    public HashMap<Integer, String> getMarks() {
        HashMap<Integer, String> marks = new HashMap<>();
        if (symbols == null) {
            parseSymTab();
        }
        for (Symbol symbol : symbols) {
            if (symbol.getType().equals("FUNC")) {
                marks.put(symbol.st_value, symbol.getName());
            }
        }
        return marks;
    }
}