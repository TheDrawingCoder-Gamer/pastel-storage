package gay.menkissing.pastelstorage.registries

import gay.menkissing.pastelstorage.PastelStorage
import gay.menkissing.pastelstorage.content.{PastelStorageBlocks, PastelStorageItems}
import gay.menkissing.pastelstorage.util.PastelStorageNumberFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.registries.{DeferredBlock, DeferredItem}

object PastelStorageTranslationKeys:
  object keys:
    object bottomlessBottle:
      object tooltip:
        val usagePickup: String = PastelStorageItems.bottomlessBottle.get().getDescriptionId + ".tooltip.usage_pickup"
        val usagePlace: String = PastelStorageItems.bottomlessBottle.get().getDescriptionId + ".tooltip.usage_place"
        
        val empty: String = PastelStorageItems.bottomlessBottle.get().getDescriptionId + ".tooltip.empty"
        
        val countMB: String = PastelStorageItems.bottomlessBottle.get().getDescriptionId + ".tooltip.count_mb"
    
    object bottomlessBattery:
      object tooltip:
        val countFE: String = PastelStorageItems.bottomlessBattery.get().getDescriptionId + ".tooltip.count_fe"

    object container:
      def make(name: String): String =
        s"container.${PastelStorage.ModId}.$name"

      def make(id: ResourceLocation): String =
        s"container.${id.getNamespace}.${id.getPath}"

      def make(holder: DeferredBlock[?]): String =
        val id = holder.getId
        make(id)

      def make(holder: DeferredItem[?]): String =
        make(holder.getId)

      val bottomlessAmphora: String = make(PastelStorageBlocks.bottomlessAmphora)
      val filterChest: String = make(PastelStorageBlocks.filterChest)
      val bottomlessBarrel: String = make(PastelStorageBlocks.bottomlessBarrel)
      val bottomlessWorm: String = make(PastelStorageBlocks.bottomlessWorm)
  
    val addedByPastelStorage: String = "book.pastelstorage.added_by_pastelstorage"
    val voidWithLava: String = "book.pastelstorage.void_with_lava"
  
  object container:
    val bottomlessAmphora: Component = Component.translatable(keys.container.bottomlessAmphora)
    val filterChest: Component = Component.translatable(keys.container.filterChest)
    val bottomlessBarrel: Component = Component.translatable(keys.container.bottomlessBarrel)
    val bottomlessWorm: Component = Component.translatable(keys.container.bottomlessWorm)

  object bottomlessBottle:
    object tooltip:
      val usagePickup: Component = Component.translatable(keys.bottomlessBottle.tooltip.usagePickup)
      val usagePlace: Component = Component.translatable(keys.bottomlessBottle.tooltip.usagePlace)
      
      val empty: Component = Component.translatable(keys.bottomlessBottle.tooltip.empty)
      def countMB(amount: Int, max: Int): Component =
        Component.translatable(keys.bottomlessBottle.tooltip.countMB, PastelStorageNumberFormatting.formatMB(amount), PastelStorageNumberFormatting.formatFluidMax(max))
  
  object bottomlessBattery:
    object tooltip:
      def countFE(amount: Int, max: Int): Component =
        Component.translatable(keys.bottomlessBattery.tooltip.countFE, PastelStorageNumberFormatting.formatFE(amount), PastelStorageNumberFormatting.formatFE(max))
