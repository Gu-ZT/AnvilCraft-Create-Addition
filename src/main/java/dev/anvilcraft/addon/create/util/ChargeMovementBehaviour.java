package dev.anvilcraft.addon.create.util;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.bearing.StabilizedBearingMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import dev.anvilcraft.addon.create.AnvilCraftCreateAddition;
import dev.dubhe.anvilcraft.api.chargecollector.ChargeCollectorManager;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现了移动行为接口，用于处理磁铁或金属方块在机械结构中运动时产生电荷的逻辑。
 */
@EventBusSubscriber(modid = AnvilCraftCreateAddition.MOD_ID)
public class ChargeMovementBehaviour implements MovementBehaviour {
    public static boolean registered = false;

    /**
     * 在服务器加载完成事件触发时注册所有符合条件的方块以启用该移动行为。
     *
     * @param event ServerStartedEvent 服务器加载完成事件
     */
    @SubscribeEvent
    public static void register(ServerStartedEvent event) {
        if (registered) {
            return;
        }
        // 注册所有属于磁铁标签或被判定为金属材质的方块
        int testCount = 0;
        List<String> registeredBlocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            String registeredBlock = ChargeMovementBehaviour.registerBlock(block);
            if (registeredBlock == null) {
                continue;
            }
            testCount++;
            registeredBlocks.add(registeredBlock);
        }
        AnvilCraftCreateAddition.LOGGER.info("Registered {} blocks with ChargeMovementBehaviour", testCount);
        AnvilCraftCreateAddition.LOGGER.info("Registered blocks: {}", registeredBlocks);
        registered = true;
    }

    private static @Nullable String registerBlock(Block block) {
        MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(block);
        if (behaviour != null) {
            return null;
        }
        String id = BuiltInRegistries.BLOCK.getKey(block).toString();
        BlockState defaultState = block.defaultBlockState();
        if (defaultState.is(ModBlockTags.MAGNET) || ChargeMovementBehaviour.isMetal(defaultState)) {
            try {
                MovementBehaviour.movementBehaviour(new ChargeMovementBehaviour()).accept(block);
                return id;
            } catch (Exception e) {
                AnvilCraftCreateAddition.LOGGER.error("Failed to register block {} with ChargeMovementBehaviour", id);
                return null;
            }
        }
        return null;
    }

    /**
     * 判断一个方块状态是否代表一种金属材料。
     *
     * @param state 方块的状态对象
     * @return 如果是金属材料返回true，否则返回false
     */
    public static boolean isMetal(BlockState state) {
        return state.is(Tags.Blocks.STORAGE_BLOCKS_COPPER) // 铜
               || state.is(Tags.Blocks.STORAGE_BLOCKS_IRON) // 铁
               || state.is(Tags.Blocks.STORAGE_BLOCKS_GOLD) // 金
               || state.is(Tags.Blocks.STORAGE_BLOCKS_NETHERITE) // 下界合金
               || state.is(Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER) // 粗铜
               || state.is(Tags.Blocks.STORAGE_BLOCKS_RAW_IRON) // 粗铁
               || state.is(Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD) // 粗金
               || state.is(ModBlockTags.STORAGE_BLOCKS_ZINC) // 锌
               || state.is(ModBlockTags.STORAGE_BLOCKS_TIN) //  锡
               || state.is(ModBlockTags.STORAGE_BLOCKS_LEAD) // 铅
               || state.is(ModBlockTags.STORAGE_BLOCKS_SILVER) // 银
               || state.is(ModBlockTags.STORAGE_BLOCKS_URANIUM) // 铀
               || state.is(ModBlockTags.STORAGE_BLOCKS_PLUTONIUM) // 钚
               || state.is(ModBlockTags.STORAGE_BLOCKS_BRONZE) // 青铜
               || state.is(ModBlockTags.STORAGE_BLOCKS_BRASS) //  黄铜
               || state.is(ModBlockTags.STORAGE_BLOCKS_TUNGSTEN) // 钨
               || state.is(ModBlockTags.STORAGE_BLOCKS_TITANIUM) // 钛
               || state.is(ModBlockTags.STORAGE_BLOCKS_RAW_URANIUM) // 粗铀
               || state.is(ModBlockTags.STORAGE_BLOCKS_RAW_TUNGSTEN) // 粗钨
               || state.is(ModBlockTags.STORAGE_BLOCKS_RAW_TITANIUM) // 粗钛
               || state.is(ModBlockTags.STORAGE_BLOCKS_RAW_ZINC) // 粗锌
               || state.is(ModBlockTags.STORAGE_BLOCKS_RAW_TIN) // 粗锡
               || state.is(ModBlockTags.STORAGE_BLOCKS_RAW_LEAD) // 粗铅
               || state.is(ModBlockTags.STORAGE_BLOCKS_RAW_SILVER) // 粗银
               || state.is(ModBlocks.ROYAL_STEEL_BLOCK) // 皇家钢
               || state.is(ModBlocks.CUT_ROYAL_STEEL_BLOCK) // 切制皇家钢
               || state.is(ModBlocks.SMOOTH_ROYAL_STEEL_BLOCK) // 平滑皇家钢
               || state.is(ModBlocks.EMBER_METAL_BLOCK) // 余烬金属
               || state.is(ModBlocks.CUT_EMBER_METAL_BLOCK) // 切制余烬金属
               || state.is(ModBlocks.FROST_METAL_BLOCK); // 浮霜金属
    }

    @Unique
    public static float calculateStressApplied(Contraption contraption) {
        float coefficient = 0.0f;
        // 遍历所有活动部件，查找具有ChargeMovementBehaviour的行为，并根据其位置累加系数
        for (MutablePair<StructureTemplate.StructureBlockInfo, MovementContext> actor : contraption.getActors()) {
            StructureTemplate.StructureBlockInfo info = actor.getLeft();
            MovementContext context = actor.getRight();
            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(info.state());
            switch (behaviour) {
                case ChargeMovementBehaviour ignored -> {
                    // 使用方块中心点到原点的距离作为影响因子
                    float offset = (float) info.pos().getCenter().length();
                    coefficient += offset;
                }
                case StabilizedBearingMovementBehaviour ignored -> {
//                    CompoundTag c = new CompoundTag();
//                    c.putLong("Pos", info.pos().asLong());
//                    c.putInt("State", id);
//
//                    BlockEntity blockEntity = ((ContraptionInvoker) contraption).invokeReadBlockEntity(context.world, info, info.nbt());
//                    if (!(blockEntity instanceof MechanicalBearingBlockEntity mechanicalBearingBlockEntity)) {
//                        continue;
//                    }
//                    ControlledContraptionEntity movedContraption = mechanicalBearingBlockEntity.getMovedContraption();
//                    if (movedContraption == null) {
//                        continue;
//                    }
//                    Contraption contraption1 = movedContraption.getContraption();
//                    if (contraption1 == null) {
//                        continue;
//                    }
//                    coefficient += ChargeMovementBehaviour.calculateStressApplied(contraption1);
                }
                case null, default -> {
                }
            }
        }
        return coefficient * AnvilCraftCreateAddition.CONFIG.stressDissipationCoefficient;
    }

    /**
     * 每tick调用一次，在移动过程中根据当前方块类型（磁铁/金属）执行不同的充电逻辑。
     *
     * @param context 移动上下文对象，包含世界、位置、状态等信息
     */
    @Override
    public void tick(MovementContext context) {
        Level level = context.world;
        double speed = context.motion.length();
        BlockPos blockPos = BlockPos.containing(context.position);
        ChargeCollectorManager instance = ChargeCollectorManager.getInstance(level);

        // 根据方块是否是磁铁决定使用哪种tick方法
        if (context.state.is(ModBlocks.MAGNET_BLOCK.get())) {
            magnetTick(instance, level, blockPos, speed);
        } else {
            metalTick(instance, level, blockPos, speed);
        }
    }

    /**
     * 处理磁铁方块移动时对其周围金属方块进行充能的操作。
     *
     * @param manager  充电管理器实例
     * @param level    当前所在的世界
     * @param blockPos 当前方块的位置
     * @param speed    方块移动的速度大小
     */
    public void magnetTick(ChargeCollectorManager manager, Level level, BlockPos blockPos, double speed) {
        // 遍历六个方向查找相邻的金属方块并为其充能
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = blockPos.relative(direction);
            BlockState offsetState = level.getBlockState(offsetPos);
            if (!ChargeMovementBehaviour.isMetal(offsetState)) {
                continue;
            }
            double surplus = AnvilCraftCreateAddition.CONFIG.chargeGeneratedEfficiency * speed;
            manager.charge(surplus, offsetPos);
        }
    }

    /**
     * 处理金属方块移动时向最近的集电器传输能量的过程。
     *
     * @param manager  充电管理器实例
     * @param level    当前所在的世界
     * @param blockPos 当前方块的位置
     * @param speed    方块移动的速度大小
     */
    public void metalTick(ChargeCollectorManager manager, Level level, BlockPos blockPos, double speed) {
        // 遍历六个方向寻找相邻的磁铁方块，并将产生的电量传递给附近的集电器
        int magnetCount = 0;
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = blockPos.relative(direction);
            BlockState offsetState = level.getBlockState(offsetPos);
            if (offsetState.is(ModBlockTags.MAGNET)) {
                magnetCount++;
            }
        }
        double surplus = AnvilCraftCreateAddition.CONFIG.chargeGeneratedEfficiency * speed * magnetCount;
        manager.charge(surplus, blockPos);
    }
}
