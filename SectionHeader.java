import java.nio.ByteBuffer;

public class SectionHeader {
    public static final int SHT_STRTAB = 0x3;
    public static final int SHT_SYMTAB = 0x2;

    public int sh_name;
    public int sh_type;
    public int sh_flags;
    public int sh_addr;
    public int sh_offset;
    public int sh_size;
    public int sh_link;
    public int sh_info;
    public int sh_addralign;
    public int sh_entsize;

    public SectionHeader(ByteBuffer buffer) {
        if (buffer.remaining() < 40) {
            throw new IllegalArgumentException("Invalid section header size");
        }
        sh_name = buffer.getInt();
        sh_type = buffer.getInt();
        sh_flags = buffer.getInt();
        sh_addr = buffer.getInt();
        sh_offset = buffer.getInt();
        sh_size = buffer.getInt();
        sh_link = buffer.getInt();
        sh_info = buffer.getInt();
        sh_addralign = buffer.getInt();
        sh_entsize = buffer.getInt();
    }
}
