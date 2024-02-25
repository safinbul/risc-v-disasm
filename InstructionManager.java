import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InstructionManager {
    ArrayList<InstructionField> instructions;
    static Map<Integer, String> REGISTER_MAP;
    HashMap<Integer, String> marks;
    private int markIndex;

    static {
        REGISTER_MAP = new HashMap<>();
        REGISTER_MAP.put(0, "zero");
        REGISTER_MAP.put(1, "ra");
        REGISTER_MAP.put(2, "sp");
        REGISTER_MAP.put(3, "gp");
        REGISTER_MAP.put(4, "tp");
        REGISTER_MAP.put(5, "t0");
        REGISTER_MAP.put(6, "t1");
        REGISTER_MAP.put(7, "t2");
        REGISTER_MAP.put(8, "s0");
        REGISTER_MAP.put(9, "s1");
        REGISTER_MAP.put(10, "a0");
        REGISTER_MAP.put(11, "a1");
        REGISTER_MAP.put(12, "a2");
        REGISTER_MAP.put(13, "a3");
        REGISTER_MAP.put(14, "a4");
        REGISTER_MAP.put(15, "a5");
        REGISTER_MAP.put(16, "a6");
        REGISTER_MAP.put(17, "a7");
        REGISTER_MAP.put(18, "s2");
        REGISTER_MAP.put(19, "s3");
        REGISTER_MAP.put(20, "s4");
        REGISTER_MAP.put(21, "s5");
        REGISTER_MAP.put(22, "s6");
        REGISTER_MAP.put(23, "s7");
        REGISTER_MAP.put(24, "s8");
        REGISTER_MAP.put(25, "s9");
        REGISTER_MAP.put(26, "s10");
        REGISTER_MAP.put(27, "s11");
        REGISTER_MAP.put(28, "t3");
        REGISTER_MAP.put(29, "t4");
        REGISTER_MAP.put(30, "t5");
        REGISTER_MAP.put(31, "t6");
    }

    public InstructionManager(HashMap<Integer, String> marks) {
        this.instructions = new ArrayList<InstructionField>();
        this.marks = marks;
        this.markIndex = 0;
    }

    public void addInstruction(int address, int value) {
        this.instructions.add(createInstruction(address, value));
    }

    private InstructionField createInstruction(int address, int value) {
        InstructionField instruction = new InstructionField(value, address);

        try {
            // Integer Register-Immediate Instructions
            if (instruction.getOpCode() == 0b0010011) {
                Map<Integer, String> operations = Map.of(0b000, "addi",
                        0b010, "slti",
                        0b011, "sltiu",
                        0b100, "xori",
                        0b110, "ori",
                        0b111, "andi");
                if (operations.containsKey(instruction.getFunct3())) {
                    instruction.setName(operations.get(instruction.getFunct3()));
                    instruction.setArgs(String.format("%s, %s, %d",
                            REGISTER_MAP.get(instruction.getRd()),
                            REGISTER_MAP.get(instruction.getRs1()),
                            toSigned(instruction.getSegment(20, 32), 12)));
                } else {
                    switch (instruction.getFunct3()) {
                        case 0b001:
                            instruction.setName("slli");
                            instruction.setArgs(String.format("%s, %s, %d",
                                    REGISTER_MAP.get(instruction.getRd()),
                                    REGISTER_MAP.get(instruction.getRs1()),
                                    instruction.getShamt()));
                            break;
                        case 0b101:
                            if (instruction.getFunct7() == 0b0000000) {
                                instruction.setName("srli");
                            } else if (instruction.getFunct7() == 0b0100000) {
                                instruction.setName("srai");
                            }
                            instruction.setArgs(String.format("%s, %s, %d",
                                    REGISTER_MAP.get(instruction.getRd()),
                                    REGISTER_MAP.get(instruction.getRs1()),
                                    instruction.getShamt()));
                            break;
                        default:
                            throw new UnknownInstructionException();

                    }
                }
                return instruction;
            }
            if (instruction.getOpCode() == 0b0110111 || instruction.getOpCode() == 0b0010111) {
                if (instruction.getOpCode() == 0b0110111) {
                    instruction.setName("lui");
                } else {
                    instruction.setName("auipc");
                }
                instruction.setArgs(String.format("%s, 0x%s",
                        REGISTER_MAP.get(instruction.getRd()),
                        Integer.toHexString(toSigned(instruction.getSegment(12, 32), 20))));
                return instruction;

            }

            // Integer Register-Register Instructions
            if (instruction.getOpCode() == 0b0110011) {
                switch ((instruction.getFunct7() << 3) + instruction.getFunct3()) {
                    case 0b0000000000:
                        instruction.setName("add");
                        break;
                    case 0b0100000000:
                        instruction.setName("sub");
                        break;
                    case 0b0000000001:
                        instruction.setName("sll");
                        break;
                    case 0b0000000010:
                        instruction.setName("slt");
                        break;
                    case 0b0000000011:
                        instruction.setName("sltu");
                        break;
                    case 0b0000000100:
                        instruction.setName("xor");
                        break;
                    case 0b0000000101:
                        instruction.setName("srl");
                        break;
                    case 0b0100000101:
                        instruction.setName("sra");
                        break;
                    case 0b0000000110:
                        instruction.setName("or");
                        break;
                    case 0b0000000111:
                        instruction.setName("and");
                        break;
                    // RV32M Standard Extension
                    case 0b0000001000:
                        instruction.setName("mul");
                        break;
                    case 0b0000001001:
                        instruction.setName("mulh");
                        break;
                    case 0b0000001010:
                        instruction.setName("mulhsu");
                        break;
                    case 0b0000001011:
                        instruction.setName("mulhu");
                        break;
                    case 0b0000001100:
                        instruction.setName("div");
                        break;
                    case 0b0000001101:
                        instruction.setName("divu");
                        break;
                    case 0b0000001110:
                        instruction.setName("rem");
                        break;
                    case 0b0000001111:
                        instruction.setName("remu");
                        break;
                    default:
                        throw new UnknownInstructionException();
                }
                instruction.setArgs(String.format("%s, %s, %s",
                        REGISTER_MAP.get(instruction.getRd()),
                        REGISTER_MAP.get(instruction.getRs1()),
                        REGISTER_MAP.get(instruction.getRs2())));
                return instruction;
            }

            // Unconditional Jumps
            if (instruction.getOpCode() == 0b1101111) {
                instruction.setName("jal");
                String dest = REGISTER_MAP.get(instruction.getRd());
                int offset = (instruction.getSegment(31, 32) << 19)
                        + (instruction.getSegment(12, 20) << 11)
                        + (instruction.getSegment(20, 21) << 10)
                        + instruction.getSegment(21, 31);
                offset = toSigned(offset << 1, 21);
                String mark;
                if (marks.containsKey(address + offset)) {
                    mark = marks.get(address + offset);
                } else {
                    mark = "L" + markIndex++;
                    marks.put(address + offset, mark);
                }
                instruction.setArgs(String.format("%s, 0x%x <%s>", dest, address + offset, mark));
                return instruction;
            }
            if (instruction.getOpCode() == 0b1100111) {
                instruction.setName("jalr");
                String dest = REGISTER_MAP.get(instruction.getRd());
                String base = REGISTER_MAP.get(instruction.getRs1());
                int offset = toSigned(instruction.getSegment(20, 32), 12);
                instruction.setArgs(String.format("%s, %d(%s)", dest, offset, base));
                return instruction;
            }

            // Conditional Branches
            if (instruction.getOpCode() == 0b1100011) {
                switch (instruction.getFunct3()) {
                    case 0b000:
                        instruction.setName("beq");
                        break;
                    case 0b001:
                        instruction.setName("bne");
                        break;
                    case 0b100:
                        instruction.setName("blt");
                        break;
                    case 0b101:
                        instruction.setName("bge");
                        break;
                    case 0b110:
                        instruction.setName("bltu");
                        break;
                    case 0b111:
                        instruction.setName("bgeu");
                        break;
                    default:
                        throw new UnknownInstructionException();
                }
                String rs1 = REGISTER_MAP.get(instruction.getRs1());
                String rs2 = REGISTER_MAP.get(instruction.getRs2());
                int offset = (instruction.getSegment(31, 32) << 11)
                        + (instruction.getSegment(7, 8) << 10)
                        + (instruction.getSegment(25, 31) << 4)
                        + (instruction.getSegment(8, 12));
                offset = toSigned(offset << 1, 13);
                String mark;
                if (marks.containsKey(address + offset)) {
                    mark = marks.get(address + offset);
                } else {
                    mark = "L" + markIndex++;
                    marks.put(address + offset, mark);
                }
                instruction.setArgs(String.format("%s, %s, 0x%x, <%s>", rs1, rs2, address + offset, mark));
                return instruction;
            }

            // Load Instructions
            if (instruction.getOpCode() == 0b0000011) {
                Map<Integer, String> operations = Map.of(0b000, "lb",
                        0b001, "lh",
                        0b010, "lw",
                        0b100, "lbu",
                        0b101, "lhu");
                if (operations.containsKey(instruction.getFunct3())) {
                    instruction.setName(operations.get(instruction.getFunct3()));
                    instruction.setArgs(String.format("%s, %d(%s)",
                            REGISTER_MAP.get(instruction.getRd()),
                            toSigned(instruction.getSegment(20, 32), 12),
                            REGISTER_MAP.get(instruction.getRs1())));
                } else {
                    throw new UnknownInstructionException();
                }
                return instruction;
            }

            // Store Instructions
            if (instruction.getOpCode() == 0b0100011) {
                Map<Integer, String> operations = Map.of(0b000, "sb",
                        0b001, "sh",
                        0b010, "sw");
                if (operations.containsKey(instruction.getFunct3())) {
                    instruction.setName(operations.get(instruction.getFunct3()));
                    instruction.setArgs(String.format("%s, %d(%s)",
                            REGISTER_MAP.get(instruction.getRs2()),
                            toSigned((instruction.getSegment(25, 32) << 5) + instruction.getSegment(7, 12), 12),
                            REGISTER_MAP.get(instruction.getRs1())));
                } else {
                    throw new UnknownInstructionException();
                }
                return instruction;
            }

            // Memory ordering instructions
            if (instruction.getOpCode() == 0b0001111) {
                if (instruction.getValue() == 8330000f) {
                    instruction.setName("fence.tso");
                } else if (instruction.getValue() == 0x100000f) {
                    // Zihintp extension
                    instruction.setName("pause");
                } else if (instruction.getFunct3() == 0b001) {
                    // Zifencei extension
                    instruction.setName("fence.i");
                } else if (instruction.getFunct3() == 0b000) {
                    instruction.setName("fence");
                    int pred = instruction.getSegment(24, 28);
                    int succ = instruction.getSegment(20, 24);
                    StringBuilder arg = new StringBuilder();
                    if (pred == 0) {
                        arg.append("0");
                    }
                    if ((pred & 0b1000) != 0) {
                        arg.append("i");
                    }
                    if ((pred & 0b0100) != 0) {
                        arg.append("o");
                    }
                    if ((pred & 0b0010) != 0) {
                        arg.append("r");
                    }
                    if ((pred & 0b0001) != 0) {
                        arg.append("w");
                    }
                    arg.append(", ");
                    if (succ == 0) {
                        arg.append("0");
                    }
                    if ((succ & 0b1000) != 0) {
                        arg.append("i");
                    }
                    if ((succ & 0b0100) != 0) {
                        arg.append("o");
                    }
                    if ((succ & 0b0010) != 0) {
                        arg.append("r");
                    }
                    if ((succ & 0b0001) != 0) {
                        arg.append("w");
                    }
                    instruction.setArgs(arg.toString());
                } else {
                    throw new UnknownInstructionException();
                }
                return instruction;
            }

            // Environment call and break instructions
            if (instruction.getOpCode() == 0b1110011) {
                if (instruction.getFunct3() == 0b000 && instruction.getFunct7() == 0b0000000) {
                    instruction.setName("ecall");
                } else if (instruction.getFunct3() == 0b000 && instruction.getFunct7() == 0b0000000) {
                    instruction.setName("ebreak");
                } else {
                    throw new UnknownInstructionException();
                }
                return instruction;
            }

            // RV32A Standard Extension
            if (instruction.getOpCode() == 0b0101111) {
                if (instruction.getFunct3() == 0b000) {
                    instruction.setName("lr.w");
                    instruction.setArgs(String.format("%s, %s", REGISTER_MAP.get(instruction.getRd()),
                            REGISTER_MAP.get(instruction.getRs1())));
                    return instruction;
                } else if (instruction.getFunct3() == 0b001) {
                    instruction.setName("sc.w");

                    instruction.setArgs(String.format("%s, %s, %s",
                            REGISTER_MAP.get(instruction.getRd()),
                            REGISTER_MAP.get(instruction.getRs1()),
                            REGISTER_MAP.get(instruction.getRs2())));
                    return instruction;
                } else if (instruction.getFunct3() == 0b010) {
                    instruction.setName("amoswap.w");
                } else if (instruction.getFunct3() == 0b011) {
                    instruction.setName("amoadd.w");
                } else if (instruction.getFunct3() == 0b100) {
                    instruction.setName("amoxor.w");
                } else if (instruction.getFunct3() == 0b101) {
                    instruction.setName("amoand.w");
                } else if (instruction.getFunct3() == 0b110) {
                    instruction.setName("amoor.w");
                } else if (instruction.getFunct3() == 0b111) {
                    instruction.setName("amomin.w");
                } else if (instruction.getFunct3() == 0b111) {
                    instruction.setName("amomax.w");
                } else if (instruction.getFunct3() == 0b111) {
                    instruction.setName("amominu.w");
                } else if (instruction.getFunct3() == 0b111) {
                    instruction.setName("amomaxu.w");
                } else {
                    throw new UnknownInstructionException();
                }
                if (instruction.getFunct3() != 0b000) {
                    instruction.setArgs(String.format("%s, %s, (%s)",
                            REGISTER_MAP.get(instruction.getRd()),
                            REGISTER_MAP.get(instruction.getRs2()),
                            REGISTER_MAP.get(instruction.getRs1())));
                }
                return instruction;
            }

            throw new UnknownInstructionException();
        } catch (UnknownInstructionException e) {
            instruction.setName("invalid_instruction");
            instruction.setArgs("");
            return instruction;
        }

    }

    public int toSigned(int value, int size) {
        if ((value & (1 << (size - 1))) != 0) {
            value -= (1 << size);
        }
        return value;
    }

    public String toPrint() {
        StringBuilder sb = new StringBuilder();
        for (InstructionField instruction : instructions) {
            if (marks.containsKey(instruction.getAddress())) {
                sb.append(String.format("\n%08x \t<%s>:\n", instruction.getAddress(),
                        marks.get(instruction.getAddress())));
            }
            sb.append(String.format("   %05x:\t%08x\t%s\n", instruction.getAddress(), instruction.getValue(),
                    instruction.toString()));
        }
        return sb.toString();
    }
}
