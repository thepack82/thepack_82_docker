package net.sf.odinms.tools;

public class IPAddressTool {

    private IPAddressTool() {
    }

    public static long dottedQuadToLong(String dottedQuad) throws RuntimeException {
        String[] quads = dottedQuad.split("\\.");
        if (quads.length != 4) {
            throw new RuntimeException("Invalid IP Address format.");
        }
        long ipAddress = 0;
        for (int i = 0; i < 4; i++) {
            int quad = Integer.parseInt(quads[i]);
            ipAddress += (long) (quad % 256) * (long) Math.pow(256, (double) (4 - i));
        }
        return ipAddress;
    }

    public static String longToDottedQuad(long longIP) throws RuntimeException {
        StringBuilder ipAddress = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int quad = (int) (longIP / (long) Math.pow(256, (double) (4 - i)));
            longIP -= (long) quad * (long) Math.pow(256, (double) (4 - i));
            if (i > 0) {
                ipAddress.append(".");
            }
            if (quad > 255) {
                throw new RuntimeException("Invalid long IP address.");
            }
            ipAddress.append(quad);
        }
        return ipAddress.toString();
    }
}
