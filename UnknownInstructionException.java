

public class UnknownInstructionException extends RuntimeException {
    public UnknownInstructionException() {
        super();
    }

    public UnknownInstructionException(int instruction) {
        super(Integer.toHexString(instruction));
    }
}
