package gay.menkissing.pastelstorage.registries.ids

import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.registries.{DeferredBlock, DeferredItem}

object PastelStorageAdvancementIds:
  def blockUnlock(block: DeferredBlock[?]): ResourceLocation =
    val r = block.getId
    ResourceLocation.fromNamespaceAndPath(r.getNamespace, s"unlocks/blocks/${r.getPath}")

  def itemUnlock(item: DeferredItem[?]): ResourceLocation =
    val r = item.getId
    ResourceLocation.fromNamespaceAndPath(r.getNamespace, s"unlocks/items/${r.getPath}")


  val bottomlessBarrelUnlock = blockUnlock(PastelStorageBlocks.bottomlessBarrel)
  val bottomlessWormUnlock = blockUnlock(PastelStorageBlocks.bottomlessWorm)
  val bottomlessAmphoraUnlock = blockUnlock(PastelStorageBlocks.bottomlessAmphora)
  val bottomlessShelfUnlock = blockUnlock(PastelStorageBlocks.bottomlessShelf)
  val filterChestUnlock = blockUnlock(PastelStorageBlocks.filterChest)
  
  val bottomlessBatteryUnlock = itemUnlock(PastelStorageItems.bottomlessBattery)
  val bottomlessBottleUnlock = itemUnlock(PastelStorageItems.bottomlessBottle)
  val toolContainerUnlock = itemUnlock(PastelStorageItems.toolContainer)
