package psidum.ugt.hardware;

public class UGTException extends Exception {
    public UGTException() {}

    public UGTException(String arg0) {
        super(arg0);
    }

    public UGTException(Throwable arg0) {
        super(arg0);
    }

    public UGTException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public UGTException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }
}
