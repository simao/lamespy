package io.simao.lamespy;

import java.util.List;

public class Location implements Comparable<Location> {
    private String name;
    private List<String> wifiNetworks;

    public Location(String name, List<String> wifiNetworks) {
        this.name = name;
        this.wifiNetworks = wifiNetworks;
    }

    public boolean includesBssid(String bssid) {
        for (String net : wifiNetworks) {
            if (net.contains(bssid)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getWifiNetworks() {
        return wifiNetworks;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "location: {name: " + name + ", locations: " + wifiNetworks.toString() + "}";
    }

    @Override
    public int compareTo(Location another) {
        return this.getName().compareTo(another.getName());
    }
}
