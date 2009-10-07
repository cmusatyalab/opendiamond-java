package edu.cmu.cs.diamond.opendiamond;

class XDR_dev_char {
    public static final int DEV_ISA_UNKNOWN = 0;
    public static final int DEV_ISA_IA32 = 1;
    public static final int DEV_ISA_IA64 = 2;
    public static final int DEV_ISA_XSCALE = 3;

    public XDR_dev_char(XDRBuffer buf) {
        isa = buf.getInt();
        speed = buf.getInt();
        mem = buf.getLong();
    }

    final int isa;
    final int speed;
    final long mem;

    @Override
    public String toString() {
        String strISA = "unknown";
        switch (isa) {
        case DEV_ISA_IA32:
            strISA = "IA32";
            break;
        case DEV_ISA_IA64:
            strISA = "IA64";
            break;
        case DEV_ISA_XSCALE:
            strISA = "XScale";
            break;
        }

        return "ISA: " + strISA + ", speed: " + speed + ", mem: " + mem;
    }
}
