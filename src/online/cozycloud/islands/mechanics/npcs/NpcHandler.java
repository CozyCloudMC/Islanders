package online.cozycloud.islands.mechanics.npcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;

public class NpcHandler {

    private final NPCRegistry REGISTRY;

    public NpcHandler() {
        REGISTRY = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
    }

    /**
     * Gets the NPCRegistry for this plugin. NPCs created with this registry will not persist after the plugin disables.
     * @return this plugin's NPCRegistry
     */
    public NPCRegistry getRegistry() {return REGISTRY;}

}
