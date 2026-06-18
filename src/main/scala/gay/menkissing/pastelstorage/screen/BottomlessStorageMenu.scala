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

class BottomlessStorageMenu(menuType: MenuType[BottomlessStorageMenu], val rows: Int, windowId: Int, override val playerInv: Inventory, override val container: Container) extends AbstractContainerMenu(menuType, windowId), CommonBottomlessStorageMenu:
  locally:
    AbstractContainerMenu.checkContainerSize(container, rows * BottomlessStorageMenu.columns)
    container.startOpen(playerInv.player)

    val k = (rows - 4) * 18
    for
      i <- 0 until rows
      j <- 0 until BottomlessStorageMenu.columns
    do
      addSlot(new CommonBottomlessStorageMenu.BoxSlot(this, container, j + i * BottomlessStorageMenu.columns, 8 + j * 18, 18 + i * 18))

    for
      i <- 0 until ScreenCommon.playerRows
      j <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, j + i * ScreenCommon.playerColumns + 9, 8 + j * 18, 103 + i * 18 + k))

    for
      i <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, i, 8 + i * 18, 161 + k))

  override def boxStart: Int = BottomlessStorageMenu.startSlot

  override def boxEnd: Int = BottomlessStorageMenu.startSlot + BottomlessStorageMenu.columns * rows


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
