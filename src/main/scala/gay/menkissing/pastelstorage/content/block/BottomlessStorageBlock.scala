package gay.menkissing.pastelstorage.content.block

import gay.menkissing.pastelstorage.content.block.entity.{BottomlessStorageBlockEntity, ContainerBottomlessStorageBlockEntity}
import gay.menkissing.pastelstorage.screen.BottomlessStorageMenu
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.{InteractionHand, InteractionResult, MenuProvider}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BarrelBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}
import net.minecraft.world.phys.BlockHitResult

abstract class BottomlessStorageBlock(val capacity: Int, props: BlockBehaviour.Properties) extends BarrelBlock(props):
  locally:
    this.registerDefaultState:
      this.stateDefinition.any()
          .setValue(BottomlessStorageBlock.FACING, Direction.NORTH)
          .setValue(BottomlessStorageBlock.OPEN, false)
      
  
  
  override def useWithoutItem(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player, blockHitResult: BlockHitResult): InteractionResult =
    if level.isClientSide then
      InteractionResult.SUCCESS
    else
      val blockEntity = level.getBlockEntity(blockPos)
      blockEntity match
        case bse: ContainerBottomlessStorageBlockEntity =>
          player.openMenu(bse)
        case _ => ()
      InteractionResult.CONSUME

  override def onRemove(replaced: BlockState, level: Level, blockPos: BlockPos, newBlock: BlockState, movedByPiston: Boolean): Unit =
    if !replaced.is(newBlock.getBlock) then
      val be = level.getBlockEntity(blockPos)
      val update =
        be match
          case bse: ContainerBottomlessStorageBlockEntity if !bse.isEmpty =>
            BottomlessStorageBlockEntity.dropContents(level, blockPos, bse)
            true
          case _ => false
      super.onRemove(replaced, level, blockPos, newBlock, movedByPiston)
      if update then
        level.updateNeighbourForOutputSignal(blockPos, this)


  override def tick(blockState: BlockState, serverLevel: ServerLevel, blockPos: BlockPos, randomSource: RandomSource): Unit =
    val be = serverLevel.getBlockEntity(blockPos)
    be match
      case sbe: ContainerBottomlessStorageBlockEntity =>
        sbe.recheckOpen()
      case _ => ()

  
  

  
          
object BottomlessStorageBlock:
  val FACING = BlockStateProperties.FACING
  val OPEN = BlockStateProperties.OPEN

  final class BottomlessBarrelBlock(props: BlockBehaviour.Properties) extends BottomlessStorageBlock(BottomlessStorageMenu.barrelContainerSize, props):
    override def newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity =
      new BottomlessStorageBlockEntity.BottomlessBarrelBlockEntity(blockPos, blockState)
  
  final class BottomlessAmphoraBlock(props: BlockBehaviour.Properties) extends BottomlessStorageBlock(BottomlessStorageMenu.amphoraContainerSize, props):
    override def newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity =
      new BottomlessStorageBlockEntity.BottomlessAmphoraBlockEntity(blockPos, blockState)