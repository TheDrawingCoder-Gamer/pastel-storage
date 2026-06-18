package gay.menkissing.pastelstorage.screen

import earth.terrarium.pastel.registries.PastelBlocks
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.PastelStorageItems
import gay.menkissing.pastelstorage.registries.{PastelStorageCriteria, PastelStorageTags}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, Slot}
import net.minecraft.world.item.ItemStack

trait CommonBottomlessStorageMenu extends AbstractContainerMenu:
  def boxStart: Int
  def boxEnd: Int

  def playerInv: Inventory
  def container: Container
  
  def tryTriggerInsertCriterion(stack: ItemStack): Unit =
    playerInv.player match
      case sp: ServerPlayer =>
        PastelStorage.Logger.debug(s"triggering insert for stack ${stack.getItemHolder.getKey.location()}")
        PastelStorageCriteria.INSERT_INTO_VOIDING_BARREL.trigger(sp, stack)
      case _ => ()


  override def quickMoveStack(player: Player, i: Int): ItemStack =
    // ported from scala 1.21 code that was
    // ported from java code that was ported from scala code that was ported from java code
    var transferredItemStack = ItemStack.EMPTY
    val slot = this.slots.get(i)
    if slot.hasItem then
      val slotStack = slot.getItem
      transferredItemStack = slotStack.copy()
      val boxStart = this.boxStart
      val boxEnd = this.boxEnd
      val invEnd = boxEnd + 36
      if i < boxEnd then
        if !super.moveItemStackTo(slotStack, boxEnd, invEnd, true) then
          return ItemStack.EMPTY
      else if !slotStack.isEmpty && CommonBottomlessStorageMenu.isValidItem(slotStack) && !super.moveItemStackTo(slotStack, boxStart, boxEnd, false) then
        return ItemStack.EMPTY

      if slotStack.isEmpty then
        slot.setByPlayer(ItemStack.EMPTY)
      else
        slot.setChanged()

      if slotStack.getCount == transferredItemStack.getCount then
        return ItemStack.EMPTY

      slot.onTake(player, slotStack)
    transferredItemStack

  

  override def stillValid(player: Player): Boolean =
    container.stillValid(player)
    
object CommonBottomlessStorageMenu:
  def isValidItem(item: ItemStack): Boolean =
    item.is(PastelBlocks.BOTTOMLESS_BUNDLE.asItem())
      || item.is(PastelStorageItems.bottomlessBottle)
      || item.is(PastelStorageItems.bottomlessBattery)
      || item.is(PastelStorageTags.item.deletesItemsWhenInsertedInto)
  
  class BoxSlot(val menu: CommonBottomlessStorageMenu, container: Container, idx: Int, x: Int, y: Int) extends Slot(container, idx, x, y):
    override def mayPlace(stack: ItemStack): Boolean =
      isValidItem(stack)

    override def setByPlayer(stack: ItemStack): Unit =
      super.setByPlayer(stack)
      if stack.is(PastelStorageTags.item.deletesItemsWhenInsertedInto) then
        menu.tryTriggerInsertCriterion(stack)