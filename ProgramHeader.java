import java.nio.ByteBuffer;

public class ProgramHeader {
    public int p_type;
    public int p_offset;
    public int p_vaddr;
    public int p_paddr;
    public int p_filesz;
    public int p_memsz;
    public int p_flags;
    public int p_align;

    public ProgramHeader(ByteBuffer buffer) {
        if (buffer.remaining() < 32) {
            throw new IllegalArgumentException("Invalid program header size");
        }
        p_type = buffer.getInt();
        p_offset = buffer.getInt();
        p_vaddr = buffer.getInt();
        p_paddr = buffer.getInt();
        p_filesz = buffer.getInt();
        p_memsz = buffer.getInt();
        p_flags = buffer.getInt();
        p_align = buffer.getInt();
    }
}
