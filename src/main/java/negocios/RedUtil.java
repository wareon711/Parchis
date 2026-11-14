package negocios;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

public final class RedUtil {

    public static final int PUERTO_BROKER = 27015;
    public static final int PUERTO_DISCOVERY = 27016;
    private static final String CARACTERES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final Random RANDOM = new Random();

    private RedUtil() {
    }

    public static String obtenerIPLocal() {
        try {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.connect(new InetSocketAddress("8.8.8.8", 10002));
                InetAddress localAddr = socket.getLocalAddress();
                if (localAddr != null && !localAddr.isLoopbackAddress() && localAddr instanceof Inet4Address) {
                    return localAddr.getHostAddress();
                }
            } catch (Exception ignored) {
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                            return ip;
                        }
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    public static String obtenerCodigo() {
        String base = codificarIP(obtenerIPLocal());
        char sufijo = CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length()));
        return base + sufijo;
    }

    public static String codificarIP(String ip) {
        try {
            String[] p = ip.trim().split("\\.");
            if (p.length != 4) return "P1000";
            int c = Integer.parseInt(p[2]) & 0xFF;
            int d = Integer.parseInt(p[3]) & 0xFF;
            int val = (((c << 8) | d) ^ 0x5A3C) & 0xFFFF;
            String b36 = Integer.toString(val + 46656, 36).toUpperCase();
            return "P" + b36;
        } catch (Exception e) {
            return "P1000";
        }
    }

    public static String resolverIP(String entrada) {
        if (entrada == null) return "127.0.0.1";
        String limpio = entrada.trim();
        if (limpio.contains(".")) {
            return limpio;
        }
        limpio = limpio.replace("-", "").replace(" ", "").toUpperCase();
        try {
            if (limpio.startsWith("P") && limpio.length() >= 5) {
                String parteIp = limpio.startsWith("PR") && limpio.length() == 6
                        ? limpio.substring(2, 6)
                        : limpio.substring(1, 5);
                int val = Integer.parseInt(parteIp, 36) - 46656;
                int orig = (val ^ 0x5A3C) & 0xFFFF;
                int c = (orig >> 8) & 0xFF;
                int d = orig & 0xFF;
                return "192.168." + c + "." + d;
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
