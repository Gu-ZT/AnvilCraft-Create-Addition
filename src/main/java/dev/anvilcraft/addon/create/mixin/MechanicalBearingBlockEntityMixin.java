package dev.anvilcraft.addon.create.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.bearing.IBearingBlockEntity;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import dev.anvilcraft.addon.create.util.ChargeMovementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

/**
 * Mixin类用于修改 {@link MechanicalBearingBlockEntity} 的行为。
 * 此类通过继承 {@link GeneratingKineticBlockEntity} 并实现接口来扩展原版逻辑，
 * 主要目的是在计算应力时考虑移动结构中特定行为的影响。
 */
@Mixin(MechanicalBearingBlockEntity.class)
abstract class MechanicalBearingBlockEntityMixin extends GeneratingKineticBlockEntity
    implements IBearingBlockEntity, IDisplayAssemblyExceptions {

    /**
     * 构造一个新的 {@code MechanicalBearingBlockEntityMixin} 实例。
     *
     * @param type  方块实体类型
     * @param pos   方块位置
     * @param state 方块状态
     */
    public MechanicalBearingBlockEntityMixin(
        BlockEntityType<?> type,
        BlockPos pos,
        BlockState state
    ) {
        super(type, pos, state);
    }

    /**
     * 获取当前被该轴承带动的机械装置实体（由Mixin注入）。
     *
     * @return 被带动的机械装置实体，可能为null
     */
    @Shadow
    public abstract @Nullable ControlledContraptionEntity getMovedContraption();

    /**
     * 计算并返回施加到此轴承上的总应力值。
     * 在原始应力基础上增加来自移动结构中带有电荷运动行为组件的额外应力贡献。
     *
     * @return 总应力值
     */
    @Override
    public float calculateStressApplied() {
        // 先调用父类方法获取基础应力值
        float calculated = super.calculateStressApplied();

        // 获取正在移动的机械装置实体及其对应的结构对象
        ControlledContraptionEntity movedContraption = this.getMovedContraption();
        if (movedContraption == null) {
            return calculated;
        }
        Contraption contraption = movedContraption.getContraption();
        if (contraption == null) {
            return calculated;
        }
        // 将计算出的附加应力加入最终结果并返回
        return calculated + ChargeMovementBehaviour.calculateStressApplied(contraption);
    }
}
