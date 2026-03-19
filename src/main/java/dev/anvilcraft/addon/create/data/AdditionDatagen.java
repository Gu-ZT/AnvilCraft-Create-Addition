package dev.anvilcraft.addon.create.data;

import com.tterrag.registrate.providers.ProviderType;
import dev.anvilcraft.addon.create.AnvilCraftCreateAddition;
import dev.anvilcraft.addon.create.data.lang.LangHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static dev.anvilcraft.addon.create.AnvilCraftCreateAddition.REGISTRATE;

@EventBusSubscriber(modid = AnvilCraftCreateAddition.MOD_ID)
public class AdditionDatagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
    }

    /**
     * 初始化生成器
     */
    public static void init() {
        REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}
