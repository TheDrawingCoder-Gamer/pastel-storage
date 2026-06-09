package gay.menkissing.pastelstorage.content.item

import gay.menkissing.pastelstorage.item.ItemBackedInventory
import gay.menkissing.pastelstorage.registries.PastelStorageTags
import gay.menkissing.pastelstorage.screen.ToolContainerMenu
import gay.menkissing.pastelstorage.screen.ToolContainerMenu.ToolContainerData
import gay.menkissing.pastelstorage.util.network.PastelStorageNetworking
import gay.menkissing.pastelstorage.util.network.PastelStorageNetworking.GayScreenHandler
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.{InteractionHand, InteractionResultHolder, SimpleContainer}
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, ClickAction, Slot}
import net.minecraft.world.item.{Item, ItemStack, ItemUtils}
import net.minecraft.world.level.Level

class ToolContainerItem(props: Item.Properties) extends Item(props):
  override def use(level: Level, player: Player, interactionHand: InteractionHand): InteractionResultHolder[ItemStack] =
    if !level.isClientSide then
      val stack = player.getItemInHand(interactionHand)
      val provider = new GayScreenHandler[ToolContainerData](ToolContainerData.STREAM_CODEC) {
        override def getOpeningData(player: ServerPlayer): ToolContainerData =
          ToolContainerData(interactionHand == InteractionHand.MAIN_HAND)

        override def getDisplayName: Component = stack.getHoverName

        override def createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
          new ToolContainerMenu(i, inventory, stack)
      }
      PastelStorageNetworking.openExtendedMenu(player, provider)
    InteractionResultHolder.sidedSuccess(player.getItemInHand(interactionHand), level.isClientSide)

  override def onDestroyed(itemEntity: ItemEntity): Unit =
    ItemUtils.onContainerDestroyed(itemEntity, ToolContainerItem.getRawInventory(itemEntity.getItem).getItems)
    itemEntity.getItem.remove(DataComponents.CONTAINER)

  override def overrideOtherStackedOnMe(thisStack: ItemStack, thatStack: ItemStack, slot: Slot, clickAction: ClickAction, player: Player, slotAccess: SlotAccess): Boolean =
    if clickAction == ClickAction.SECONDARY && slot.allowModification(player) && !thatStack.isEmpty && thatStack.is(PastelStorageTags.item.validToolTag) then
      val container = ToolContainerItem.getRawInventory(thisStack)
      if container.canAddItem(thatStack) then
        val res = container.addItem(thatStack)
        slotAccess.set(res)
        return true
    false

  override def overrideStackedOnOther(thisStack: ItemStack, slot: Slot, clickAction: ClickAction, player: Player): Boolean =
    if clickAction != ClickAction.SECONDARY then
      false
    else
      val container = ToolContainerItem.getRawInventory(thisStack)
      val thatStack = slot.getItem
      if !thatStack.isEmpty && thatStack.is(PastelStorageTags.item.validToolTag) then
        if container.canAddItem(thatStack) then
          val res = container.addItem(thatStack)
          slot.set(res)
          true
        else
          false
      else
        false

object ToolContainerItem:
  def getRawInventory(stack: ItemStack): SimpleContainer =
    new ItemBackedInventory(stack, ToolContainerMenu.containerSize)
