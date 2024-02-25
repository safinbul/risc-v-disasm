public class InstructionField {

    private final int value;
    private final int address;
    private String name;
    private String argString;

    public InstructionField(int value, int address) {
        this.value = value;
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    public int getValue() {
        return value;
    }

    public int getOpCode() {
        return value & 0x7F;
    }

    public int getRd() {
        return (value >> 7) & 0x1F;
    }

    public int getFunct3() {
        return (value >> 12) & 0x7;
    }

    public int getRs1() {
        return (value >> 15) & 0x1F;
    }

    public int getRs2() {
        return (value >> 20) & 0x1F;
    }

    public int getFunct7() {
        return (value >> 25) & 0x7F;
    }

    public int getShamt() {
        return (value >> 20) & 0x1F;
    }

    public int getSegment(int low, int high) {
        if (low >= high) {
            throw new IllegalArgumentException("Low >= high");
        }
        return (value >>> low) & ((1 << (high - low)) - 1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArgs(String argString) {
        this.argString = argString;
    }

    @Override
    public String toString() {
        if (this.argString != null) {
            return String.format("%7s\t%s", this.name, this.argString);
        } else {
            return String.format("%7s", this.name);
        }
    }
}
