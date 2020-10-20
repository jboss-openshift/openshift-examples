import java.security.Security;

public class TestDNSCaching
{
    public static void main(String[] args)
    {
        final String s = "networkaddress.cache.negative.ttl";
        System.out.println(s + "=" + Security.getProperty(s));
    }
}
