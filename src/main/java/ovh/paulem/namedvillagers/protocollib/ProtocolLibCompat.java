package ovh.paulem.namedvillagers.protocollib;

import com.comphenix.protocol.ProtocolLibrary;

public class ProtocolLibCompat {
    public void enable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListenerVillagerInventory());
    }
}
