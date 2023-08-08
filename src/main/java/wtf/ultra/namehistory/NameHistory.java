package wtf.ultra.namehistory;

import net.weavemc.loader.api.ModInitializer;
import net.weavemc.loader.api.command.CommandBus;

public class NameHistory implements ModInitializer {
    @Override
    public void preInit() {
        CommandBus.register(new HistoryCommand());
    }
}