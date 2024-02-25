import java.nio.ByteBuffer;

public class Elf32Header {
    public byte[] e_ident;
    public short e_type;
    public short e_machine;
    public int e_version;
    public int e_entry;
    public int e_phoff;
    public int e_shoff;
    public int e_flags;
    public short e_ehsize;
    public short e_phentsize;
    public short e_phnum;
    public short e_shentsize;
    public short e_shnum;
    public short e_shstrndx;

    public Elf32Header(ByteBuffer header) {
        if (header.remaining() < 52) {
            throw new IllegalArgumentException("Invalid ELF header size");
        }
        e_ident = new byte[16];
        header.get(e_ident);

        e_type = header.getShort();
        if (e_type != 2) {
            throw new IllegalArgumentException("Invalid ELF type");
        }
        e_machine = header.getShort();
        // RISC-V machine code
        if (e_machine != 0xF3) {
            throw new IllegalArgumentException("Invalid ELF machine");
        }
        e_version = header.getInt();
        e_entry = header.getInt();
        e_phoff = header.getInt();
        e_shoff = header.getInt();
        e_flags = header.getInt();
        e_ehsize = header.getShort();
        e_phentsize = header.getShort();
        e_phnum = header.getShort();
        e_shentsize = header.getShort();
        e_shnum = header.getShort();
        e_shstrndx = header.getShort();
    }
}
