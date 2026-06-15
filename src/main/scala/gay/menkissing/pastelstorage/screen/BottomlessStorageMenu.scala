package gay.menkissing.pastelstorage.screen

import earth.terrarium.pastel.registries.PastelBlocks
import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.PastelStorageItems
import gay.menkissing.pastelstorage.registries.{PastelStorageCriteria, PastelStorageScreens, PastelStorageTags}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, MenuType, Slot}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.{Container, SimpleContainer}

class BottomlessStorageMenu(menuType: MenuType[BottomlessStorageMenu], val rows: Int, windowId: Int, playerInv: Inventory, val container: Container) extends AbstractContainerMenu(menuType, windowId):
  locally:
    AbstractContainerMenu.checkContainerSize(container, rows * BottomlessStorageMenu.columns)
    container.startOpen(playerInv.player)

    val k = (rows - 4) * 18
    for
      i <- 0 until rows
      j <- 0 until BottomlessStorageMenu.columns
    do
      addSlot(new Slot(container, j + i * BottomlessStorageMenu.columns, 8 + j * 18, 18 + i * 18) {
        override def mayPlace(itemStack: ItemStack): Boolean =
          isValidItem(itemStack)

        override def setByPlayer(stack : ItemStack): Unit =
          super.setByPlayer(stack)
          if stack.is(PastelStorageTags.item.deletesItemsWhenInsertedInto) then
            tryTriggerInsertCriterion(stack)
      })

    for
      i <- 0 until ScreenCommon.playerRows
      j <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, j + i * ScreenCommon.playerColumns + 9, 8 + j * 18, 103 + i * 18 + k))

    for
      i <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, i, 8 + i * 18, 161 + k))

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
      val boxStart = BottomlessStorageMenu.startSlot
      val boxEnd = boxStart + BottomlessStorageMenu.columns * rows
      val invEnd = boxEnd + 36
      if i < boxEnd then
        if !moveItemStackTo(slotStack, boxEnd, invEnd, true) then
          return ItemStack.EMPTY
      else
        if !slotStack.isEmpty && isValidItem(slotStack) && !moveItemStackTo(slotStack, boxStart, boxEnd, false) then
          return ItemStack.EMPTY

      if slotStack.isEmpty then
        slot.setByPlayer(ItemStack.EMPTY)
      else
        slot.setChanged()

      if slotStack.getCount == transferredItemStack.getCount then
        return ItemStack.EMPTY

      slot.onTake(player, slotStack)
    transferredItemStack

  def isValidItem(item: ItemStack): Boolean =
    item.is(PastelBlocks.BOTTOMLESS_BUNDLE.asItem()) 
      || item.is(PastelStorageItems.bottomlessBottle)
      || item.is(PastelStorageItems.bottomlessBattery)
      || item.is(PastelStorageTags.item.deletesItemsWhenInsertedInto)
  override def stillValid(player: Player): Boolean =
    container.stillValid(player)

object BottomlessStorageMenu:
  val startSlot = 0
  val amphoraRows = 6
  val barrelRows = 3
  val columns = 9
  val amphoraContainerSize = amphoraRows * columns
  val barrelContainerSize = barrelRows * columns
  
  private def bindConstructor(screen: MenuType[BottomlessStorageMenu], rows: Int)(windowId: Int, playerInv: Inventory): BottomlessStorageMenu =
    new BottomlessStorageMenu(screen, rows, windowId, playerInv, new SimpleContainer(rows * 9))
  
  def barrel(windowId: Int, playerInv: Inventory): BottomlessStorageMenu =
    bindConstructor(PastelStorageScreens.bottomlessBarrel.get(), barrelRows)(windowId, playerInv)
    
  def barrelServer(windowId: Int, playerInv: Inventory, container: Container): BottomlessStorageMenu =
    new BottomlessStorageMenu(PastelStorageScreens.bottomlessBarrel.get(), barrelRows, windowId, playerInv, container)
    
  def amphora(windowId: Int, playerInv: Inventory): BottomlessStorageMenu =
    bindConstructor(PastelStorageScreens.bottomlessAmphora.get(), amphoraRows)(windowId, playerInv)
  
  def amphoraServer(windowId: Int, playerInv: Inventory, container: Container): BottomlessStorageMenu =
    new BottomlessStorageMenu(PastelStorageScreens.bottomlessAmphora.get(), amphoraRows, windowId, playerInv, container)
