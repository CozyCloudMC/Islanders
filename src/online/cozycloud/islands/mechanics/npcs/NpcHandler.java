package online.cozycloud.islands.mechanics.npcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;

public class NpcHandler {

    private final NPCRegistry REGISTRY;

    public NpcHandler() {
        REGISTRY = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
    }

}
