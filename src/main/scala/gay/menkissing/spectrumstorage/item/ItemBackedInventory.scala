package gay.menkissing.spectrumstorage.item

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.{CompoundTag, ListTag, Tag}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents

class ItemBackedInventory(val stack: ItemStack, expectedSize: Int) extends SimpleContainer(expectedSize):
  locally:
    val contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    contents.copyInto(getItems)

  override def stillValid(player: Player): Boolean =
    !stack.isEmpty

  override def setChanged(): Unit =
    super.setChanged()
   
    val contents = ItemContainerContents.fromItems(getItems)
    stack.set(DataComponents.CONTAINER, contents)
