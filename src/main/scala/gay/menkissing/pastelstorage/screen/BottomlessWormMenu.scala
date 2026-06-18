package gay.menkissing.pastelstorage.screen

import gay.menkissing.pastelstorage.registries.PastelStorageScreens
import gay.menkissing.pastelstorage.screen.BottomlessStorageMenu.{amphoraRows, barrelRows, bindConstructor}
import net.minecraft.world.{Container, SimpleContainer}
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.{AbstractContainerMenu, DispenserMenu, MenuType, Slot}
class BottomlessWormMenu(menuType: MenuType[BottomlessWormMenu], windowId: Int, override val playerInv: Inventory, override val container: Container) extends AbstractContainerMenu(menuType, windowId), CommonBottomlessStorageMenu:
  locally:
    AbstractContainerMenu.checkContainerSize(container, 9)
    container.startOpen(playerInv.player)

    for
      i <- 0 until 3
      j <- 0 until 3
    do
      addSlot(new CommonBottomlessStorageMenu.BoxSlot(this, container, j + i * 3, 62 + j * 18, 17 + i * 18))

    for
      i <- 0 until ScreenCommon.playerRows
      j <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, j + i * ScreenCommon.playerColumns + 9, 8 + j * 18, 84 + i * 18))

    for
      i <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, i, 8 + i * 18, 142))

  override def boxStart: Int = 0

  override def boxEnd: Int = 9


object BottomlessWormMenu:

  def client(windowId: Int, playerInv: Inventory): BottomlessWormMenu =
    new BottomlessWormMenu(PastelStorageScreens.bottomlessWorm.get(), windowId, playerInv, new SimpleContainer(9))

  def server(windowId: Int, playerInv: Inventory, container: Container): BottomlessWormMenu =
    new BottomlessWormMenu(PastelStorageScreens.bottomlessWorm.get(), windowId, playerInv, container)
