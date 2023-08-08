package wtf.ultra.namehistory;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
    modid = ModEvent.MOD_ID,
    version = ModEvent.VERSION
)
public class ModEvent {
    public static final String MOD_ID = "namehistory";
    public static final String VERSION = "1.8";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new NameHistory());
    }
}



