package gay.menkissing.pastelstorage.content.block

import com.mojang.serialization.MapCodec
import gay.menkissing.pastelstorage.content.block.entity.{BottomlessStorageBlockEntity, BottomlessWormBlockEntity, ContainerBottomlessStorageBlockEntity, WithoutOpenContainerBottomlessStorageBlockEntity}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.{BarrelBlock, BaseEntityBlock, Block, DirectionalBlock, EntityBlock}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult

class BottomlessWormBlock(props: BlockBehaviour.Properties) extends DirectionalBlock(props), EntityBlock:
  locally:
    this.registerDefaultState:
      this.stateDefinition.any()
          .setValue(BottomlessWormBlock.FACING, Direction.UP)

  override def useWithoutItem(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player, blockHitResult: BlockHitResult): InteractionResult =
    if level.isClientSide then
      InteractionResult.SUCCESS
    else
      val blockEntity = level.getBlockEntity(blockPos)
      blockEntity match
        case bse: BottomlessWormBlockEntity =>
          player.openMenu(bse)
        case _ => ()
      InteractionResult.CONSUME

  override def onRemove(replaced: BlockState, level: Level, blockPos: BlockPos, newBlock: BlockState, movedByPiston: Boolean): Unit =
    if !replaced.is(newBlock.getBlock) then
      val be = level.getBlockEntity(blockPos)
      val update =
        be match
          case bse: BottomlessWormBlockEntity if !bse.isEmpty =>
            BottomlessStorageBlockEntity.dropContents(level, blockPos, bse)
            true
          case _ => false
      super.onRemove(replaced, level, blockPos, newBlock, movedByPiston)
      if update then
        level.updateNeighbourForOutputSignal(blockPos, this)

  override def newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity =
    new BottomlessWormBlockEntity(blockPos, blockState)

  override def codec(): MapCodec[BottomlessWormBlock] = BottomlessWormBlock.CODEC

  protected override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit =
    builder.add(BottomlessWormBlock.FACING)

  override def getStateForPlacement(ctx : BlockPlaceContext): BlockState =
    this.defaultBlockState().setValue(BottomlessWormBlock.FACING, ctx.getNearestLookingDirection)

object BottomlessWormBlock:
  val FACING = BlockStateProperties.FACING
  val CODEC = BlockBehaviour.simpleCodec(BottomlessWormBlock.apply)