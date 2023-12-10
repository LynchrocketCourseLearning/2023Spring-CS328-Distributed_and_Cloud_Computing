package myrmi.registry;

import myrmi.Remote;
import myrmi.exception.AlreadyBoundException;
import myrmi.exception.NotBoundException;
import myrmi.exception.RemoteException;

import java.net.URI;
import java.net.URISyntaxException;

public class Naming {
    private Naming() {
    }

    private static Registry getRegistry(ParsedNamingURL parsed) {
        return LocateRegistry.getRegistry(parsed.host, parsed.port);
    }

    public static Remote lookup(String str)
            throws URISyntaxException,
            NotBoundException,
            RemoteException {
        ParsedNamingURL parsed = parseURL(str);
        Registry registry = getRegistry(parsed);
        if (parsed.name == null)
            return registry;
        return registry.lookup(parsed.name);
    }

    public static void bind(String str, Remote obj)
            throws URISyntaxException,
            RemoteException,
            AlreadyBoundException {
        if (obj == null)
            throw new NullPointerException("Cannot bind to null");
        ParsedNamingURL parsed = parseURL(str);
        Registry registry = getRegistry(parsed);
        registry.bind(parsed.name, obj);
    }

    public static void rebind(String str, Remote obj)
            throws URISyntaxException,
            RemoteException {
        if (obj == null)
            throw new NullPointerException("Cannot bind to null");
        ParsedNamingURL parsed = parseURL(str);
        Registry registry = getRegistry(parsed);
        registry.rebind(parsed.name, obj);
    }

    public static void unbind(String str)
            throws URISyntaxException,
            NotBoundException,
            RemoteException {
        ParsedNamingURL parsed = parseURL(str);
        Registry registry = getRegistry(parsed);
        registry.unbind(parsed.name);
    }

    private static ParsedNamingURL parseURL(String str)
            throws URISyntaxException {
        URI uri = new URI(str);
        String name = uri.getPath();
        if (name != null) {
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (name.length() == 0) {
                name = null;
            }
        }
        String host = uri.getHost();
        if (host == null) {
            host = Registry.REGISTRY_HOST;
        }
        int port = uri.getPort();
        if (port <= 0) {
            port = Registry.REGISTRY_PORT;
        }
        return new ParsedNamingURL(host, port, name);
    }

    private static class ParsedNamingURL {
        String host;
        int port;
        String name;

        ParsedNamingURL(String host, int port, String name) {
            this.host = host;
            this.port = port;
            this.name = name;
        }
    }
}
