package dev.anvilcraft.addon.create.init;

import com.simibubi.create.AllDamageTypes;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import dev.anvilcraft.addon.create.AnvilCraftCreateAddition;
import dev.dubhe.anvilcraft.api.amulet.type.AmuletType;
import dev.dubhe.anvilcraft.init.ModRegistries;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class AdditionAmuletTypes {
    private static final DeferredRegister<AmuletType> REGISTER = DeferredRegister.create(
        ModRegistries.AMULET_TYPE_KEY,
        AnvilCraftCreateAddition.MOD_ID
    );

    public static final DeferredHolder<AmuletType, ? extends AmuletType> COGWHEEL = REGISTER.register(
        "cogwheel", AmuletType.builder()
            .obtainByDamage(
                AllDamageTypes.CRUSH,
                AllDamageTypes.CUCKOO_SURPRISE,
                AllDamageTypes.DRILL,
                AllDamageTypes.POTATO_CANNON,
                AllDamageTypes.ROLLER,
                AllDamageTypes.RUN_OVER,
                AllDamageTypes.SAW
            )
            .obtainOr((player, source) -> source.typeHolder().is(DamageTypes.PLAYER_ATTACK) && Optional.ofNullable(source.getEntity())
                .map(entity -> Util.instanceOfAny(entity, DeployerFakePlayer.class))
                .orElse(false))
            .immuneDamageFromObtain()
            .amulet(AdditionItems.COGWHEEL_AMULET)
            ::build
    );

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
    }
}
