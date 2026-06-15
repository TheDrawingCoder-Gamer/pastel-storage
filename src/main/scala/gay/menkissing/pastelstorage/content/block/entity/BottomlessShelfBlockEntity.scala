package gay.menkissing.pastelstorage.content.block.entity

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.block.BottomlessShelfBlock
import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent

import java.util.Objects

class BottomlessShelfBlockEntity(pos: BlockPos, state: BlockState) extends BottomlessStorageBlockEntity(6, PastelStorageBlocks.bottomlessShelfBlockEntity.get(), pos, state):

  override def updateSlotShown(slot: Int): Unit =

    if slot >= 0 && slot < capacity then
      this.lastInteractedSlot = slot
      var blockState = this.getBlockState

      BottomlessShelfBlock.SHELF_SLOT_OCCUPIED_PROPS.zipWithIndex.foreach: (prop, i) =>
        val kind =
          val item = items.get(i)
          if item.isEmpty then
            BottomlessShelfBlock.ShelfSlotOccupiedBy.Empty
          else if item.is(PastelStorageItems.bottomlessBottle) then
            BottomlessShelfBlock.ShelfSlotOccupiedBy.Bottle
          else if item.is(PastelStorageItems.bottomlessBattery) then
            BottomlessShelfBlock.ShelfSlotOccupiedBy.Battery
          else
            BottomlessShelfBlock.ShelfSlotOccupiedBy.Bundle
        blockState = blockState.setValue(prop, kind)
      Objects.requireNonNull(this.level).setBlockAndUpdate(this.worldPosition, blockState)
      this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.worldPosition, GameEvent.Context.of(blockState))
    else
      PastelStorage.Logger.error("Expected slot to be 0-5, got {}", slot)