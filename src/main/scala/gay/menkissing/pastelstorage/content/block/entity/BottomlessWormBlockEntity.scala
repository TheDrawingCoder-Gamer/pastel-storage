package gay.menkissing.pastelstorage.content.block.entity

import gay.menkissing.pastelstorage.content.PastelStorageBlocks
import gay.menkissing.pastelstorage.content.block.BottomlessWormBlock
import gay.menkissing.pastelstorage.content.block.entity.BottomlessStorageBlockEntity.TItemStorages
import gay.menkissing.pastelstorage.registries.PastelStorageTranslationKeys
import gay.menkissing.pastelstorage.screen.BottomlessWormMenu
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.items.IItemHandler

class BottomlessWormBlockEntity(pos: BlockPos, state: BlockState) extends WithoutOpenContainerBottomlessStorageBlockEntity(9, PastelStorageBlocks.bottomlessWormBlockEntity.get(), pos, state):
  override val itemStorage: TItemStorages = new WormItemStorages

  final class WormItemStorages extends TItemStorages:
    override def parent: BottomlessStorageBlockEntity = BottomlessWormBlockEntity.this

    def getNext: Option[IItemHandler] =
      val facing = getBlockState.getValue(BottomlessWormBlock.FACING)
      val nextPos = pos.relative(facing)
      Option(Capabilities.ItemHandler.BLOCK.getCapability(getLevel, nextPos, null, null, facing.getOpposite))

    override def getSlots: Int =
      super.getSlots + getNext.map(_.getSlots).getOrElse(0)

    override def getSlotLimit(slot: Int): Int =
      if slot < capacity then
        super.getSlotLimit(slot)
      else
        getNext.map(_.getSlotLimit(slot - capacity)).getOrElse(0)

    override def getStackInSlot(slot: Int): ItemStack =
      if slot < capacity then
        super.getStackInSlot(slot)
      else
        getNext.map(_.getStackInSlot(slot - capacity)).getOrElse(ItemStack.EMPTY)

    override def insertItem(slot: Int, stack: ItemStack, simulated: Boolean): ItemStack =
      if slot < capacity then
        super.insertItem(slot, stack, simulated)
      else
        getNext.map(_.insertItem(slot - capacity, stack, simulated)).getOrElse(stack)

    override def extractItem(slot: Int, amount: Int, simulated: Boolean): ItemStack =
      if slot < capacity then
        super.extractItem(slot, amount, simulated)
      else
        getNext.map(_.extractItem(slot - capacity, amount, simulated)).getOrElse(ItemStack.EMPTY)


  override def defaultName: Component = PastelStorageTranslationKeys.container.bottomlessWorm

  override def createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
    BottomlessWormMenu.server(i, inventory, containerView)

