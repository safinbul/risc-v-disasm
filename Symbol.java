import java.nio.ByteBuffer;

public class Symbol {
    public final int st_name;
    public final int st_value;
    public final int st_size;
    public final byte st_info;
    public final byte st_other;
    public final short st_shndx;

    private String name;

    public Symbol(ByteBuffer buffer) {
        st_name = buffer.getInt();
        st_value = buffer.getInt();
        st_size = buffer.getInt();
        st_info = buffer.get();
        st_other = buffer.get();
        st_shndx = buffer.getShort();
        this.name = "";
    }

    public String getType() {
        byte type = (byte) (st_info & 0x0F);
        switch (type) {
            case 0:
                return "NOTYPE";
            case 1:
                return "OBJECT";
            case 2:
                return "FUNC";
            case 3:
                return "SECTION";
            case 4:
                return "FILE";
            case 5:
                return "COMMON";
            case 6:
                return "TLS";
            case 10:
                return "LOOS";
            case 12:
                return "HIOS";
            case 13:
                return "LOPROC";
            case 15:
                return "HIPROC";
            default:
                return "UNKNOWN";
        }
    }

    public String getBind() {
        byte bind = (byte) ((st_info >> 4) & 0x0F);
        switch (bind) {
            case 0:
                return "LOCAL";
            case 1:
                return "GLOBAL";
            case 2:
                return "WEAK";
            case 10:
                return "LOOS";
            case 12:
                return "HIOS";
            case 13:
                return "LOPROC";
            case 15:
                return "HIPROC";
            default:
                return "UNKNOWN";
        }
    }

    public String getVis() {
        byte vis = (byte) (st_other & 0x03);
        switch (vis) {
            case 0:
                return "DEFAULT";
            case 1:
                return "INTERNAL";
            case 2:
                return "HIDDEN";
            case 3:
                return "PROTECTED";
            default:
                return "UNKNOWN";
        }
    }

    public String getIndex() {
        if (st_shndx < 0) {
            return "ABS";
        }
        return st_shndx == 0 ? "UNDEF" : String.valueOf(st_shndx);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
